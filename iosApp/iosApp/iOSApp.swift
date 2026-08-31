import SwiftUI
import Headless
import ProximityReader
import sharedKit

/// Bridges a single message into the shared Kotlin `AppLogger`, which mirrors it to NSLog and the on-disk log file.
private func logInfo(_ message: String) {
    AppLogger.shared.info(objects: KotlinArray<AnyObject>(size: 1) { _ in message as AnyObject })
}

private func logError(_ message: String) {
    AppLogger.shared.error(objects: KotlinArray<AnyObject>(size: 1) { _ in message as AnyObject })
}

// MARK: - Reader Session Lifecycle Helpers

@MainActor
private func checkAndPrepareReader(using svc: HeadlessService) async {
    let status = await svc.getReaderStatus()
    logInfo("MineSec reader status check: \(status)")

    if status.accountStatus == Headless.HeadlessReaderAccountStatus.linked {
        // Only trigger warm-up if the session is not currently ready (e.g., Session Expired)
        if status.readiness != Headless.HeadlessReaderReadiness.ready {
            logInfo("MineSec Session not ready or expired. Executing warm-up...")
            await executeWarmUp(using: svc)
        } else {
            logInfo("MineSec Session is already active and ready. Proceeding...")
        }
    } else if status.accountStatus == Headless.HeadlessReaderAccountStatus.notLinked {
        // Needs linking first
        logInfo("MineSec Account not linked. Linking now...")
        let linkResult = await svc.linkAccount()
        logInfo("MineSec linkResult: \(linkResult)")
        
        switch linkResult {
        case .success(let s):
            logInfo("MineSec account linked — \(s.accountStatus)")
            // Warm up only AFTER link succeeds
            await executeWarmUp(using: svc)
            
        case .failure(let e):
            logError("MineSec link account failed: \(e)")
        }
    }
}

@MainActor
private func executeWarmUp(using svc: HeadlessService) async {
    let warmUpResult = await svc.warmUp()
    logInfo("MineSec warmUpResult: \(warmUpResult)")
    
    switch warmUpResult {
    case .success(let s):
        logInfo("MineSec warm-up OK — readiness: \(s.readiness)")
    case .failure(let e):
        logError("MineSec warm-up failed: \(e)")
    }
}

// MARK: - App Delegate & Main App

// Controls supported interface orientations for the whole app.
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        supportedInterfaceOrientationsFor window: UIWindow?
    ) -> UIInterfaceOrientationMask {
        return .portrait
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    private let mineSecService = HeadlessService()
    private let bridgeProvider: SwiftMineSecProvider
    private let keychainProvider = SwiftKeychainProvider()
    private let tapToPayReaderStatusProvider = SwiftTapToPayReaderStatusProvider()
    
    init() {
        let provider = SwiftMineSecProvider(service: mineSecService)
        self.bridgeProvider = provider

        let svc = mineSecService

        // Snapshot @State values into plain locals — avoids capturing `self` in the Task closure.
        let businessName = "Pertok"
        let merchantId = "M1770891455"
        let merchantCategoryCode = "5999"
        let terminalEmvProfileId = "4c702000-0000-0000-03c9-91c46a350fd8"

        Task {
            let result = await svc.initSoftPOS(
                profileId: "prof_01KY416J4K1MN0SFXDKM8DT7FR",
                licenseFile: "payment-options.license",
                posConfiguration: POSConfiguration(
                    tapToPayReaderConfig: TapToPayReaderConfiguration(
                        merchantBusinessName: businessName,
                        merchantId: merchantId,
                        merchantCategoryCode: merchantCategoryCode,
                        terminalEmvProfileId: terminalEmvProfileId
                    )
                )
            )
            
            logInfo("Minesec result initSoftPOS: \(result)")
            
            switch result {
            case .success(let info):
                logInfo("MineSec initialized: \(info.headlessId) v\(info.headlessVersion)")
                provider.isInitialized = true
                
                // Unified check and warm-up logic
                await checkAndPrepareReader(using: svc)

                Task {
                    for await event in await svc.events {
                        logInfo("MineSec Event: \(event)")
                    }
                }

            case .failure(let error):
                logError("MineSec Init failed: \(error)")
            }
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView(
                provider: bridgeProvider,
                keychainProvider: keychainProvider,
                tapToPayReaderStatusProvider: tapToPayReaderStatusProvider
            )
        }
    }
}

// MARK: - Providers

class SwiftKeychainProvider: NSObject, KeychainProvider {
    func get(key: String) -> String? {
        return KeychainHelper.get(key)
    }

    func put(key: String, value: String) {
        KeychainHelper.put(key, value: value)
    }
}

/// Bridges Apple's ProximityReader Tap to Pay reader status to Kotlin's `getNfcStatus()`.
/// ProximityReader is a Swift-only framework (no Kotlin/Native cinterop), so eligibility and
/// runtime readiness are checked natively here and injected via `TapToPayReaderStatusHelper`.
class SwiftTapToPayReaderStatusProvider: NSObject, TapToPayReaderStatusProvider {
    /// Set from the `PaymentCardReader.events` stream once a reader has been observed.
    /// `nil` until the first event arrives, since there is no synchronous availability query.
    private var latestIsReady: Bool?
    private var probeTask: Task<Void, Never>?

    func isSupported() -> Bool {
        return PaymentCardReader.isSupported
    }

    func isReady() -> Bool {
        guard PaymentCardReader.isSupported else { return false }
        startProbeIfNeeded()
        return latestIsReady ?? false
    }

    /// Lazily creates a `PaymentCardReader` and watches its event stream for `.readyForTap`/`.notReady`
    /// so `isReady()` can answer synchronously from the most recently observed state.
    private func startProbeIfNeeded() {
        guard probeTask == nil else { return }
        let reader = PaymentCardReader()
        probeTask = Task { [weak self] in
            for await event in reader.events {
                logInfo("MineSec [TapToPayReaderStatusProvider] reader event: \(event)")
                switch event {
                case .readyForTap:
                    self?.latestIsReady = true
                case .notReady:
                    self?.latestIsReady = false
                default:
                    break
                }
            }
        }
    }
}

/// Bridges Kotlin [MineSecPaymentProvider] calls to the native MineSec Headless iOS SDK v1.2.00.
class SwiftMineSecProvider: NSObject, MineSecPaymentProvider {
    private let service: HeadlessService
    /// Set to true by iOSApp after initSoftPOS completes successfully.
    var isInitialized: Bool = false

    init(service: HeadlessService) {
        self.service = service
        super.init()
    }

    func launchSale(
        amount: String,
        currency: String,
        description: String?,
        posReference: String,
        onResult: @escaping (MineSecTransactionResult) -> Void
    ) {
        Task {
            // Check session and automatically recover if expired before proceeding
            await checkAndPrepareReader(using: service)
            
            let poi = PoiRequest(
                tranType: .sale,
                amount: Amount(value: amount, currency: currency),
                profileId: "prof_01KY416J4K1MN0SFXDKM8DT7FR",
                description: description,
                posReference: posReference,
                cvmSignatureMode: .signOnPaper
            )

            let result = await service.launchRequest(poi)
            logInfo("MineSec launchSale result: \(result)")
            DispatchQueue.main.async {
                switch result {
                case .success(let response):
                    let encoder = JSONEncoder()
                    let jsonData = try? encoder.encode(response)
                    let jsonString = jsonData.flatMap { String(data: $0, encoding: .utf8) }

                    onResult(MineSecTransactionResult(
                        success: true,
                        tranId: response.tranId,
                        posReference: posReference,
                        acquirerResponseJson: jsonString,
                        tranType: "SALE",
                        errorMessage: nil
                    ))

                case .failure(let error):
                    onResult(MineSecTransactionResult(
                        success: false,
                        tranId: nil,
                        posReference: posReference,
                        acquirerResponseJson: nil,
                        tranType: nil,
                        errorMessage: error.localizedDescription
                    ))
                }
            }
        }
    }

    func launchVoid(
        acquirerTransactionId: String,
        profileId: String,
        onResult: @escaping (MineSecTransactionResult) -> Void
    ) {
        Task {
            // Check session and automatically recover if expired before proceeding
            await checkAndPrepareReader(using: service)

            let result = await service.actionVoid(tranId: acquirerTransactionId)
            logInfo("MineSec launchVoid result: \(result)")
            DispatchQueue.main.async {
                switch result {
                case .success(let response):
                    onResult(MineSecTransactionResult(
                        success: true,
                        tranId: response.tranId,
                        posReference: nil,
                        acquirerResponseJson: nil,
                        tranType: "VOID",
                        errorMessage: nil
                    ))
                case .failure(let error):
                    onResult(MineSecTransactionResult(
                        success: false,
                        tranId: nil,
                        posReference: nil,
                        acquirerResponseJson: nil,
                        tranType: "VOID",
                        errorMessage: error.localizedDescription
                    ))
                }
            }
        }
    }

    func launchLinkedRefund(
        acquirerTransactionId: String,
        amount: String,
        currency: String,
        profileId: String,
        onResult: @escaping (MineSecTransactionResult) -> Void
    ) {
        Task {
            // Check session and automatically recover if expired before proceeding
            await checkAndPrepareReader(using: service)
            
            let amountObj = Amount(value: amount, currency: currency)
            let result = await service.actionLinkedRefund(tranId: acquirerTransactionId, amount: amountObj)
            logInfo("MineSec launchLinkedRefund result: \(result)")
            DispatchQueue.main.async {
                switch result {
                case .success(let response):
                    onResult(MineSecTransactionResult(
                        success: true,
                        tranId: response.tranId,
                        posReference: nil,
                        acquirerResponseJson: nil,
                        tranType: "REFUND",
                        errorMessage: nil
                    ))
                case .failure(let error):
                    onResult(MineSecTransactionResult(
                        success: false,
                        tranId: nil,
                        posReference: nil,
                        acquirerResponseJson: nil,
                        tranType: "REFUND",
                        errorMessage: error.localizedDescription
                    ))
                }
            }
        }
    }
}

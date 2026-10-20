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

// MARK: - Reader Coordinator (serializes warm-up/invalidate, tracks session consumption)

/// Owns all reader prep so `launchSale`/`launchVoid`/`launchLinkedRefund` and the MineSec
/// event observer never race each other's invalidate/warmUp sequence — concurrent callers
/// await the same in-flight prepare instead of starting their own.
///
/// Also tracks session consumption explicitly: MineSec's `getReaderStatus()` keeps
/// reporting `.ready` even after a session has already been used for a tap (accountStatus
/// stays `.linked`, readiness doesn't reliably flip back). Trusting that status alone meant
/// every transaction after the first one skipped invalidate/warm-up entirely and handed
/// Apple's ProximityReader a session that could never register a new tap — producing a
/// timeout that looked like "worked once, then broke."
actor ReaderCoordinator {
    private let svc: HeadlessService
    private let statusProvider: SwiftTapToPayReaderStatusProvider
    private var inFlight: Task<Bool, Never>?
    private var sessionConsumed = false

    init(svc: HeadlessService, statusProvider: SwiftTapToPayReaderStatusProvider) {
        self.svc = svc
        self.statusProvider = statusProvider
    }

    /// Call after ANY transaction result (success or failure) — both consume the armed
    /// reader session and require a fresh warm-up before the next tap.
    func markConsumed() {
        sessionConsumed = true
    }

    func prepare() async -> Bool {
        if let inFlight {
            return await inFlight.value
        }
        let task = Task { await self.checkAndPrepareReader() }
        inFlight = task
        let result = await task.value
        inFlight = nil
        return result
    }

    private func checkAndPrepareReader() async -> Bool {
        let status = await svc.getReaderStatus()
        logInfo("MineSec reader status check: \(status)")

        guard status.accountStatus == .linked else {
            logError("MineSec account is not linked")
            await statusProvider.updateReadiness(false)
            return false
        }

        if status.readiness == .ready && !sessionConsumed {
            logInfo("MineSec Session is already active and ready")
            await statusProvider.updateReadiness(true)
            return true
        }

        logInfo("MineSec Session not ready/expired/consumed. Invalidating stale session before warm-up...")
        await svc.invalidateSession()

        // Poll instead of a fixed sleep: retries up to ~2s, adapting to how long
        // ProximityReader actually takes to tear down instead of gambling on a fixed delay.
        let warmUpResult = await Self.warmUpWithRetry(svc: svc, attempts: 4, delayNs: 500_000_000)
        logInfo("MineSec warmUpResult: \(warmUpResult)")

        switch warmUpResult {
        case .success(let newStatus):
            logInfo("MineSec warm-up OK — readiness: \(newStatus.readiness)")
            sessionConsumed = false // fresh session, not yet used

            let statusNow = await svc.getReaderStatus()
            logInfo("MineSec new reader status check after warm up: \(statusNow)")

            let ready = newStatus.accountStatus == .linked && newStatus.readiness == .ready
            await statusProvider.updateReadiness(ready)
            return ready

        case .failure(let error):
            logError("MineSec warm-up failed: \(error)")
            await statusProvider.updateReadiness(false)
            return false
        }
    }

    // NOTE: `Result<ReaderStatus, MineSecError>` below is inferred from the original code's
    // usage pattern, not confirmed against the Headless SDK's actual declared return type for
    // `warmUp()`. Verify this against the SDK before relying on it — the error type name in
    // particular may not match what MineSec actually exports.
    private static func warmUpWithRetry(
        svc: HeadlessService,
        attempts: Int,
        delayNs: UInt64
    ) async -> Result<HeadlessReaderStatus, HeadlessError> {
        var lastResult: Result<HeadlessReaderStatus, HeadlessError>!
        for attempt in 1...attempts {
            lastResult = await svc.warmUp()
            if case .success(let status) = lastResult, status.readiness == .ready {
                return lastResult
            }
            if attempt < attempts {
                try? await Task.sleep(nanoseconds: delayNs)
            }
        }
        return lastResult
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
    private let tapToPayReaderStatusProvider: SwiftTapToPayReaderStatusProvider
    private let readerCoordinator: ReaderCoordinator

    init() {
        let statusProvider = SwiftTapToPayReaderStatusProvider()
        self.tapToPayReaderStatusProvider = statusProvider

        let svc = mineSecService
        let coordinator = ReaderCoordinator(svc: svc, statusProvider: statusProvider)
        self.readerCoordinator = coordinator

        let provider = SwiftMineSecProvider(service: svc, coordinator: coordinator)
        self.bridgeProvider = provider

        // Snapshot @State values into plain locals — avoids capturing `self` in the Task closure.
        let businessName = "Pertok"
        let merchantId = "M1770891455"
        let merchantCategoryCode = "5999"
        let terminalEmvProfileId = "4c702000-0000-0000-03c9-91c46a350fd8"

        Task {
            let result = await svc.initSoftPOS(
                profileId: "prof_01M0Y76NPJ4EEKSS83M1NCHKG1",
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

                // Unified check and warm-up logic, now serialized through the coordinator.
                _ = await coordinator.prepare()

                Task {
                    for await event in await svc.events {
                        logInfo("MineSec Event: \(event)")
                        switch event {
                        case .notReady:
                            logInfo("MineSec reader reported notReady — resetting session")
                            await statusProvider.updateReadiness(false)
                            _ = await coordinator.prepare()
                        case .readyForTap:
                            await statusProvider.updateReadiness(true)
                        default:
                            break
                        }
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

/// Actor-backed store for reader readiness, safe under concurrent access from
/// launchSale/launchVoid/launchLinkedRefund and the MineSec event-observer Task.
actor SwiftTapToPayReaderStatusProviderCore {
    private var latestIsReady: Bool = false

    func isReady() -> Bool { latestIsReady }
    func updateReadiness(_ isReady: Bool) { latestIsReady = isReady }
}

/// Bridges MineSec's Tap to Pay reader status to Kotlin's `getNfcStatus()`.
///
/// Readiness is driven by `HeadlessService.getReaderStatus()`/`warmUp()` via
/// `ReaderCoordinator`, not by a standalone `PaymentCardReader` instance: per MineSec's
/// api-guide.md, `PaymentCardReader`/`ProximityReader` internals are not the supported
/// customer API, and a `PaymentCardReader` created independently of the Headless SDK never
/// has an active read session on it, so it never emits `.readyForTap` — that previously made
/// `isReady()` return false forever.
///
/// `TapToPayReaderStatusProvider`'s protocol is presumably synchronous, so `isReady()`
/// exposes a cached value while all real reads/writes go through the actor core.
class SwiftTapToPayReaderStatusProvider: NSObject, TapToPayReaderStatusProvider {
    private let core = SwiftTapToPayReaderStatusProviderCore()
    private var cachedIsReady: Bool = false

    func isSupported() -> Bool {
        return PaymentCardReader.isSupported
    }

    func isReady() -> Bool {
        return cachedIsReady
    }

    func updateReadiness(_ isReady: Bool) async {
        await core.updateReadiness(isReady)
        cachedIsReady = isReady
    }
}

/// Bridges Kotlin [MineSecPaymentProvider] calls to the native MineSec Headless iOS SDK v1.2.00.
class SwiftMineSecProvider: NSObject, MineSecPaymentProvider {
    private let service: HeadlessService
    private let coordinator: ReaderCoordinator
    /// Set to true by iOSApp after initSoftPOS completes successfully.
    var isInitialized: Bool = false

    init(service: HeadlessService, coordinator: ReaderCoordinator) {
        self.service = service
        self.coordinator = coordinator
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
            // Check session and automatically recover if expired/consumed before proceeding.
            let ready = await coordinator.prepare()

            guard ready else {
                logError("MineSec reader is not ready. launchPayment aborted.")
                DispatchQueue.main.async {
                    onResult(MineSecTransactionResult(
                        success: false,
                        tranId: nil,
                        posReference: posReference,
                        acquirerResponseJson: nil,
                        tranType: nil,
                        errorMessage: "MineSec Reader is not ready. Please try again."
                    ))
                }
                return
            }

            logInfo("MineSec launchSale started")

            let poi = PoiRequest(
                tranType: .sale,
                amount: Amount(value: amount, currency: currency),
                profileId: "prof_01M0Y76NPJ4EEKSS83M1NCHKG1",
                description: description,
                posReference: posReference,
                cvmSignatureMode: .signOnPaper
            )

            // launchRequest(_:) is the legacy alias; launchPayment(_:) is MineSec's
            // recommended API going forward (see ms-registry-ios api-guide.md).
            let result = await service.launchRequest(poi)
            logInfo("MineSec launchSale result: \(result)")

            // Either outcome consumes the armed reader session — next call must re-prepare.
            await coordinator.markConsumed()

            DispatchQueue.main.async {
                switch result {
                case .success(let response):
                    let encoder = JSONEncoder()
                    var jsonString: String? = nil
                    do {
                        let jsonData = try encoder.encode(response)
                        jsonString = String(data: jsonData, encoding: .utf8)
                    } catch {
                        logError("MineSec launchSale: failed to encode acquirer response: \(error)")
                    }

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
            // Check session and automatically recover if expired/consumed before proceeding.
            let ready = await coordinator.prepare()

            guard ready else {
                logError("MineSec reader is not ready. actionVoid aborted.")
                DispatchQueue.main.async {
                    onResult(MineSecTransactionResult(
                        success: false,
                        tranId: nil,
                        posReference: nil,
                        acquirerResponseJson: nil,
                        tranType: "VOID",
                        errorMessage: "MineSec Reader is not ready. Please try again."
                    ))
                }
                return
            }

            let result = await service.actionVoid(tranId: acquirerTransactionId)
            logInfo("MineSec launchVoid result: \(result)")

            await coordinator.markConsumed()

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
            // Check session and automatically recover if expired/consumed before proceeding.
            let ready = await coordinator.prepare()

            guard ready else {
                logError("MineSec reader is not ready. actionLinkedRefund aborted.")
                DispatchQueue.main.async {
                    onResult(MineSecTransactionResult(
                        success: false,
                        tranId: nil,
                        posReference: nil,
                        acquirerResponseJson: nil,
                        tranType: "REFUND",
                        errorMessage: "MineSec Reader is not ready. Please try again."
                    ))
                }
                return
            }

            let amountObj = Amount(value: amount, currency: currency)
            let result = await service.actionLinkedRefund(tranId: acquirerTransactionId, amount: amountObj)
            logInfo("MineSec launchLinkedRefund result: \(result)")

            await coordinator.markConsumed()

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

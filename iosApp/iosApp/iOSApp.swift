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

// MARK: - Reader Manager (owns the Tap to Pay reader lifecycle, implements TapToPayReaderBridge)

/// Owns the entire MineSec reader lifecycle (init / warm-up / teardown) behind the shared
/// Kotlin `TapToPayReader` contract. Started exactly once, from shared Kotlin's
/// `ReaderLifecycleCoordinator` (via `ReaderManagerBridge`) — never from `iOSApp.init()`.
///
/// Also tracks session consumption explicitly: MineSec's `getReaderStatus()` keeps
/// reporting `.ready` even after a session has already been used for a tap (accountStatus
/// stays `.linked`, readiness doesn't reliably flip back). Trusting that status alone meant
/// every transaction after the first one skipped invalidate/warm-up entirely and handed
/// Apple's ProximityReader a session that could never register a new tap — producing a
/// timeout that looked like "worked once, then broke."
actor ReaderManager {
    struct MerchantConfig: Equatable {
        let profileId: String
        let merchantBusinessName: String
        let merchantId: String
        let merchantCategoryCode: String
        let terminalEmvProfileId: String
    }

    struct StatusSnapshot {
        let kind: String // notInitialized, initializing, configuring, notLinked, ready, failed
        let progress: Int32
        let message: String?
        var isReady: Bool { kind == "ready" }
    }

    private let svc: HeadlessService
    /// Legacy boolean readiness mirror still consumed by `getNfcStatus()` call sites that
    /// weren't moved onto `ensureReadyForPayment()` in this step — kept in sync so that
    /// out-of-scope UI (e.g. the NFC-not-enabled early checks) keeps working unchanged.
    private let legacyStatusProvider: SwiftTapToPayReaderStatusProvider

    private var currentConfig: MerchantConfig?
    private var started = false
    private var sessionConsumed = false
    private var inFlightEnsure: Task<StatusSnapshot, Never>?
    private var eventObserverTask: Task<Void, Never>?
    private var stateListener: TapToPayReaderBridgeStateListener?
    private var onInitializedChanged: (@Sendable (Bool) -> Void)?
    /// Most recent terminal read-session event (`.readCancelled`/`.readNotCompleted`), cleared on
    /// `.readyForTap`/`.readCompleted`. Lets `SwiftMineSecProvider` classify a failed transaction
    /// as cancelled/timed-out from a real SDK signal instead of string-matching error text (5.9).
    private var lastTerminalReadEvent: HeadlessReaderEvent?

    init(svc: HeadlessService, legacyStatusProvider: SwiftTapToPayReaderStatusProvider) {
        self.svc = svc
        self.legacyStatusProvider = legacyStatusProvider
    }

    func setStateListener(_ listener: TapToPayReaderBridgeStateListener) {
        self.stateListener = listener
    }

    /// Lets `SwiftMineSecProvider` mirror `initSoftPOS` success/failure into its
    /// Kotlin-visible `isInitialized` flag.
    func setOnInitializedChanged(_ callback: @escaping @Sendable (Bool) -> Void) {
        self.onInitializedChanged = callback
    }

    /// No-op if already started with an identical config; otherwise stops the current
    /// session first, then calls `initSoftPOS` with the new one.
    func start(config: MerchantConfig) async {
        if started, currentConfig == config {
            logInfo("ReaderManager.start: already started with identical config — no-op")
            return
        }
        if started {
            logInfo("ReaderManager.start: config changed — stopping current session first")
            await stopInternal()
        }

        currentConfig = config
        started = true
        updateState(kind: "initializing", progress: 0, message: nil)

        let result = await svc.initSoftPOS(
            profileId: config.profileId,
            licenseFile: "payment-options.license",
            posConfiguration: POSConfiguration(
                tapToPayReaderConfig: TapToPayReaderConfiguration(
                    merchantBusinessName: config.merchantBusinessName,
                    merchantId: config.merchantId,
                    merchantCategoryCode: config.merchantCategoryCode,
                    terminalEmvProfileId: config.terminalEmvProfileId
                )
            )
        )

        switch result {
        case .success(let info):
            logInfo("MineSec initialized: \(info.headlessId) v\(info.headlessVersion)")
            onInitializedChanged?(true)
            observeEvents()
            _ = await ensureReadyForPayment()

        case .failure(let error):
            logError("MineSec Init failed: \(error)")
            onInitializedChanged?(false)
            updateState(kind: "failed", progress: 0, message: "\(error)")
        }
    }

    func stop() async {
        await stopInternal()
    }

    private func stopInternal() async {
        eventObserverTask?.cancel()
        eventObserverTask = nil
        // HeadlessService exposes no teardown/dispose API (confirmed against the SDK's full
        // public surface and its README quickstart) — invalidateSession() is the closest
        // available primitive. It invalidates the current reader session but cannot undo
        // initSoftPOS; a later start(config:) re-initializes via a fresh initSoftPOS call.
        await svc.invalidateSession()
        started = false
        currentConfig = nil
        sessionConsumed = false
        onInitializedChanged?(false)
        updateState(kind: "notInitialized", progress: 0, message: nil)
    }

    func onForeground() async {
        guard started else { return }
        _ = await ensureReadyForPayment()
    }

    /// Call after ANY transaction result (success or failure) — both consume the armed
    /// reader session and require a fresh warm-up before the next tap.
    func markConsumed() {
        sessionConsumed = true
    }

    @discardableResult
    func ensureReadyForPayment() async -> StatusSnapshot {
        if let inFlightEnsure {
            return await inFlightEnsure.value
        }
        let task = Task { await self.checkAndPrepareReader() }
        inFlightEnsure = task
        let result = await task.value
        inFlightEnsure = nil
        return result
    }

    /// Drives Apple's Tap to Pay on iPhone account-linking (Terms & Conditions) UI via
    /// `HeadlessService.linkAccount()`. Always refreshes the full reader state afterwards so
    /// observers (checkout, Settings, the enable flow itself) see a consistent snapshot.
    @discardableResult
    func linkAccount() async -> StatusSnapshot {
        guard started else {
            return updateState(kind: "notInitialized", progress: 0, message: nil)
        }

        logInfo("MineSec linkAccount started")
        let result = await svc.linkAccount()

        switch result {
        case .success(let status):
            logInfo("MineSec linkAccount succeeded: \(status)")
            guard status.accountStatus == .linked else {
                return updateState(kind: "notLinked", progress: 0, message: nil)
            }
            // Linked — immediately try to warm up so readiness reflects reality.
            return await checkAndPrepareReader()

        case .failure(let error):
            logError("MineSec linkAccount failed: \(error)")
            let message: String
            switch error {
            case .accountLinkRequired:
                message = "You need to accept Apple's Tap to Pay on iPhone terms to continue."
            case .accountLinkDeclined:
                message = "Setup was declined. You can try again anytime."
            case .accountLinkFailed(let reason):
                message = "Couldn't set up Tap to Pay on iPhone: \(reason)"
            default:
                message = "\(error)"
            }
            return updateState(kind: "failed", progress: 0, message: message)
        }
    }

    private func checkAndPrepareReader() async -> StatusSnapshot {
        guard started else {
            return updateState(kind: "notInitialized", progress: 0, message: nil)
        }

        let readerStatus = await svc.getReaderStatus()
        logInfo("MineSec reader status check: \(readerStatus)")

        guard readerStatus.accountStatus == .linked else {
            logError("MineSec account is not linked")
            return updateState(kind: "notLinked", progress: 0, message: nil)
        }

        if readerStatus.readiness == .ready, !sessionConsumed {
            logInfo("MineSec session is already active and ready")
            return updateState(kind: "ready", progress: 0, message: nil)
        }

        logInfo("MineSec session not ready/expired/consumed. Invalidating stale session before warm-up...")
        await svc.invalidateSession()

        // Poll instead of a fixed sleep: retries up to ~2s, adapting to how long
        // ProximityReader actually takes to tear down instead of gambling on a fixed delay.
        let warmUpResult = await Self.warmUpWithRetry(svc: svc, attempts: 4, delayNs: 500_000_000)
        logInfo("MineSec warmUpResult: \(warmUpResult)")

        switch warmUpResult {
        case .success(let newStatus):
            sessionConsumed = false // fresh session, not yet used
            guard newStatus.accountStatus == .linked else {
                return updateState(kind: "notLinked", progress: 0, message: nil)
            }
            guard newStatus.readiness == .ready else {
                return updateState(kind: "failed", progress: 0, message: "Reader not ready after warm-up")
            }
            return updateState(kind: "ready", progress: 0, message: nil)

        case .failure(let error):
            logError("MineSec warm-up failed: \(error)")
            return updateState(kind: "failed", progress: 0, message: "\(error)")
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

    private func observeEvents() {
        eventObserverTask?.cancel()
        eventObserverTask = Task { [weak self] in
            guard let self else { return }
            for await event in await svc.events {
                await self.handle(event: event)
            }
        }
    }

    private func handle(event: HeadlessReaderEvent) {
        logInfo("MineSec Event: \(event)")
        switch event {
        case .notReady:
            logInfo("MineSec reader reported notReady")
            updateState(kind: "notLinked", progress: 0, message: nil)
        case .readyForTap:
            updateState(kind: "ready", progress: 0, message: nil)
            lastTerminalReadEvent = nil
        case .progress(let percent):
            updateState(kind: "configuring", progress: Int32(percent), message: nil)
        case .readCancelled, .readNotCompleted:
            lastTerminalReadEvent = event
        case .readCompleted:
            lastTerminalReadEvent = nil
        default:
            break
        }
    }

    /// Classifies the most recent terminal read-session event into a stable outcome kind:
    /// "cancelled" when the user backed out of Apple's native Tap to Pay sheet, "timeout" when
    /// no card was presented in time / retries were exhausted. Returns nil when neither
    /// applies — the eventual outcome then comes from the transaction result itself (a real
    /// decline, webhook failure, etc.). Consuming resets it so a stale event from a previous
    /// tap can never leak into the next one's classification.
    func consumeLastOutcomeKind() -> String? {
        defer { lastTerminalReadEvent = nil }
        switch lastTerminalReadEvent {
        case .readCancelled:
            return "cancelled"
        case .readNotCompleted:
            return "timeout"
        default:
            return nil
        }
    }

    @discardableResult
    private func updateState(kind: String, progress: Int32, message: String?) -> StatusSnapshot {
        let snapshot = StatusSnapshot(kind: kind, progress: progress, message: message)
        stateListener?.onStateChanged(kind: kind, progress: progress, message: message)
        Task { await legacyStatusProvider.updateReadiness(snapshot.isReady) }
        return snapshot
    }
}

/// Wraps [ReaderManager] behind Kotlin's `TapToPayReaderBridge` protocol — primitive-only,
/// callback-based methods, matching the existing `MineSecPaymentProvider` pattern, since a
/// Swift class implementing a Kotlin-defined protocol can't conform to Kotlin `suspend fun`s.
final class ReaderManagerBridge: NSObject, TapToPayReaderBridge {
    private let manager: ReaderManager

    init(manager: ReaderManager) {
        self.manager = manager
    }

    func start(
        profileId: String,
        merchantBusinessName: String,
        merchantId: String,
        merchantCategoryCode: String,
        terminalEmvProfileId: String
    ) {
        let config = ReaderManager.MerchantConfig(
            profileId: profileId,
            merchantBusinessName: merchantBusinessName,
            merchantId: merchantId,
            merchantCategoryCode: merchantCategoryCode,
            terminalEmvProfileId: terminalEmvProfileId
        )
        Task { await manager.start(config: config) }
    }

    func stop() {
        Task { await manager.stop() }
    }

    func onForeground() {
        Task { await manager.onForeground() }
    }

    func ensureReadyForPayment(completion: @escaping (String, KotlinInt, String?) -> Void) {
        Task {
            let snapshot = await manager.ensureReadyForPayment()
            completion(snapshot.kind, KotlinInt(int: snapshot.progress), snapshot.message)
        }
    }

    func linkAccount(completion: @escaping (String, KotlinInt, String?) -> Void) {
        Task {
            let snapshot = await manager.linkAccount()
            completion(snapshot.kind, KotlinInt(int: snapshot.progress), snapshot.message)
        }
    }

    func setStateListener(listener: TapToPayReaderBridgeStateListener) {
        Task { await manager.setStateListener(listener) }
    }
}

// MARK: - Tap to Pay Discovery (Apple's native merchant-education content)

/// Wraps Apple's `ProximityReaderDiscovery` (iOS 18+ only — matches this app's 18.5 deployment
/// target, so there's no older-OS fallback path to support) to present the native "How to Tap"
/// content, covering the merchant-education requirements (4.5/4.6/4.7) without any bespoke UI.
final class ProximityReaderDiscoveryPresenter: NSObject, TapToPayDiscoveryBridge {
    private let discovery = ProximityReaderDiscovery()

    func presentHowToTap(completion: @escaping (KotlinBoolean, String?) -> Void) {
        Task {
            do {
                let content = try await discovery.content(for: .payment(.howToTap))
                guard let topVC = Self.topViewController() else {
                    logError("ProximityReaderDiscovery: no view controller available to present from")
                    DispatchQueue.main.async {
                        completion(KotlinBoolean(bool: false), "No view controller available to present from.")
                    }
                    return
                }
                try await discovery.presentContent(content, from: topVC)
                DispatchQueue.main.async {
                    completion(KotlinBoolean(bool: true), nil)
                }
            } catch {
                logError("ProximityReaderDiscovery failed: \(error)")
                DispatchQueue.main.async {
                    completion(KotlinBoolean(bool: false), error.localizedDescription)
                }
            }
        }
    }

    private static func topViewController() -> UIViewController? {
        guard let rootVC = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .first(where: { $0.activationState == .foregroundActive })?
            .keyWindow?.rootViewController
        else {
            return nil
        }
        var topVC = rootVC
        while let presented = topVC.presentedViewController {
            topVC = presented
        }
        return topVC
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
    private let readerManagerBridge: ReaderManagerBridge
    private let tapToPayDiscoveryPresenter = ProximityReaderDiscoveryPresenter()

    init() {
        let statusProvider = SwiftTapToPayReaderStatusProvider()
        self.tapToPayReaderStatusProvider = statusProvider

        let svc = mineSecService
        let manager = ReaderManager(svc: svc, legacyStatusProvider: statusProvider)
        self.readerManagerBridge = ReaderManagerBridge(manager: manager)

        let provider = SwiftMineSecProvider(service: svc, readerManager: manager)
        self.bridgeProvider = provider

        // iOSApp only registers the reader manager with shared Kotlin code here — it must
        // never start the SDK itself. Starting (and stopping, and foreground warm-up) is
        // driven exclusively by shared Kotlin's `ReaderLifecycleCoordinator`, in response to
        // login / session-restore / logout / app-foreground events.
    }

    var body: some Scene {
        WindowGroup {
            ContentView(
                provider: bridgeProvider,
                keychainProvider: keychainProvider,
                tapToPayReaderStatusProvider: tapToPayReaderStatusProvider,
                tapToPayReaderBridge: readerManagerBridge,
                tapToPayDiscoveryBridge: tapToPayDiscoveryPresenter
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
/// `ReaderManager`, not by a standalone `PaymentCardReader` instance: per MineSec's
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
    private let readerManager: ReaderManager
    /// Set to true by iOSApp after initSoftPOS completes successfully.
    var isInitialized: Bool = false

    init(service: HeadlessService, readerManager: ReaderManager) {
        self.service = service
        self.readerManager = readerManager
        super.init()

        let setInitialized: @Sendable (Bool) -> Void = { [weak self] isInitialized in
            self?.isInitialized = isInitialized
        }
        Task { await readerManager.setOnInitializedChanged(setInitialized) }
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
            let snapshot = await readerManager.ensureReadyForPayment()

            guard snapshot.isReady else {
                logError("MineSec reader is not ready. launchPayment aborted.")
                DispatchQueue.main.async {
                    onResult(MineSecTransactionResult(
                        success: false,
                        tranId: nil,
                        posReference: posReference,
                        acquirerResponseJson: nil,
                        tranType: nil,
                        errorMessage: "MineSec Reader is not ready. Please try again.",
                        outcomeKind: "error"
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
            await readerManager.markConsumed()

            // Classify from the real reader-session event observed during this tap, if any
            // (cancelled/timeout) — not from string-matching error.localizedDescription (5.9).
            let outcomeKind = await readerManager.consumeLastOutcomeKind()

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
                        errorMessage: nil,
                        outcomeKind: nil
                    ))

                case .failure(let error):
                    onResult(MineSecTransactionResult(
                        success: false,
                        tranId: nil,
                        posReference: posReference,
                        acquirerResponseJson: nil,
                        tranType: nil,
                        errorMessage: error.localizedDescription,
                        outcomeKind: outcomeKind
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
            let snapshot = await readerManager.ensureReadyForPayment()

            guard snapshot.isReady else {
                logError("MineSec reader is not ready. actionVoid aborted.")
                DispatchQueue.main.async {
                    onResult(MineSecTransactionResult(
                        success: false,
                        tranId: nil,
                        posReference: nil,
                        acquirerResponseJson: nil,
                        tranType: "VOID",
                        errorMessage: "MineSec Reader is not ready. Please try again.",
                        outcomeKind: "error"
                    ))
                }
                return
            }

            let result = await service.actionVoid(tranId: acquirerTransactionId)
            logInfo("MineSec launchVoid result: \(result)")

            await readerManager.markConsumed()

            DispatchQueue.main.async {
                switch result {
                case .success(let response):
                    onResult(MineSecTransactionResult(
                        success: true,
                        tranId: response.tranId,
                        posReference: nil,
                        acquirerResponseJson: nil,
                        tranType: "VOID",
                        errorMessage: nil,
                        outcomeKind: nil
                    ))
                case .failure(let error):
                    onResult(MineSecTransactionResult(
                        success: false,
                        tranId: nil,
                        posReference: nil,
                        acquirerResponseJson: nil,
                        tranType: "VOID",
                        errorMessage: error.localizedDescription,
                        outcomeKind: nil
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
            let snapshot = await readerManager.ensureReadyForPayment()

            guard snapshot.isReady else {
                logError("MineSec reader is not ready. actionLinkedRefund aborted.")
                DispatchQueue.main.async {
                    onResult(MineSecTransactionResult(
                        success: false,
                        tranId: nil,
                        posReference: nil,
                        acquirerResponseJson: nil,
                        tranType: "REFUND",
                        errorMessage: "MineSec Reader is not ready. Please try again.",
                        outcomeKind: "error"
                    ))
                }
                return
            }

            let amountObj = Amount(value: amount, currency: currency)
            let result = await service.actionLinkedRefund(tranId: acquirerTransactionId, amount: amountObj)
            logInfo("MineSec launchLinkedRefund result: \(result)")

            await readerManager.markConsumed()

            DispatchQueue.main.async {
                switch result {
                case .success(let response):
                    onResult(MineSecTransactionResult(
                        success: true,
                        tranId: response.tranId,
                        posReference: nil,
                        acquirerResponseJson: nil,
                        tranType: "REFUND",
                        errorMessage: nil,
                        outcomeKind: nil
                    ))
                case .failure(let error):
                    onResult(MineSecTransactionResult(
                        success: false,
                        tranId: nil,
                        posReference: nil,
                        acquirerResponseJson: nil,
                        tranType: "REFUND",
                        errorMessage: error.localizedDescription,
                        outcomeKind: nil
                    ))
                }
            }
        }
    }
}

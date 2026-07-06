import SwiftUI
import Headless
import sharedKit

@main
struct iOSApp: App {
    private let mineSecService = HeadlessService()
    private let bridgeProvider: SwiftMineSecProvider

    init() {
        self.bridgeProvider = SwiftMineSecProvider(service: mineSecService)

        let svc = mineSecService
        Task {
            // Initialize the MineSec Headless SDK v1.2.00
            let result = await svc.initSoftPOS(
                profileId: ConstantsKt.MINESEC_PROFILE_ID,
                licenseFile: "payment-options.license"  // base name; SDK resolves payment-options.license
            )

            switch result {
            case .success(let info):
                print("MineSec initialized: \(info.headlessId) v\(info.headlessVersion)")

                // Pre-warm the NFC reader so the first payment is faster.
                let warmUpResult = await svc.warmUp()
                switch warmUpResult {
                case .success(let status):
                    print("MineSec warm-up OK — readiness: \(status.readiness), account: \(status.accountStatus)")
                    if status.accountStatus == .notLinked {
                        let linkResult = await svc.linkAccount()
                        switch linkResult {
                        case .success(let s): print("MineSec account linked — \(s.accountStatus)")
                        case .failure(let e): print("MineSec link account failed: \(e)")
                        }
                    }
                case .failure(let e):
                    print("MineSec warm-up failed: \(e)")
                }

                // Observe reader events for diagnostics.
                Task {
                    for await event in svc.events {
                        print("MineSec Event: \(event)")
                    }
                }

            case .failure(let error):
                print("MineSec Init failed: \(error)")
            }
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView(provider: bridgeProvider)
        }
    }
}

/// Bridges Kotlin [MineSecPaymentProvider] calls to the native MineSec Headless iOS SDK v1.2.00.
class SwiftMineSecProvider: NSObject, MineSecPaymentProvider {
    private let service: HeadlessService

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
            let poi = PoiRequest(
                tranType: .sale,
                amount: Amount(value: amount, currency: currency),
                profileId: ConstantsKt.MINESEC_PROFILE_ID,
                description: description,
                posReference: posReference,
                cvmSignatureMode: .signOnPaper
            )

            let result = await service.launchRequest(poi)

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
            let result = await service.actionVoid(tranId: acquirerTransactionId)
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
            let amountObj = Amount(value: amount, currency: currency)
            let result = await service.actionLinkedRefund(tranId: acquirerTransactionId, amount: amountObj)
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

import SwiftUI
import Headless
import sharedKit

// MineSec SoftPOS profile ID — must match the activated profile in the MineSec dashboard.
private let MINESEC_PROFILE_ID = "prof_01KH8NQC4PVFKRNH31ZPC2QJNN"

@main
struct iOSApp: App {
    private let mineSecService = HeadlessService()
    private let bridgeProvider: SwiftMineSecProvider

    init() {
        self.bridgeProvider = SwiftMineSecProvider(service: mineSecService)

        // Capture the actor reference directly to avoid escaping-closure-captures-self error
        // (App is a struct, so self is mutating and cannot be captured by escaping closures).
        let svc = mineSecService
        Task {
            let result = await svc.initSoftPOS(
                profileId: MINESEC_PROFILE_ID,
                licenseFile: "payment-options.license"
            )

            switch result {
            case .success(let info):
                print("MineSec initialized: \(info.headlessId)")
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

/// Bridges Kotlin [MineSecPaymentProvider] calls to the native MineSec Headless iOS SDK v1.0.3.
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
                profileId: MINESEC_PROFILE_ID,
                description: description,
                posReference: posReference,
                cvmSignatureMode: .signOnPaper
            )
            
            let result = await service.launchRequest(poi)
            
            DispatchQueue.main.async {
                switch result {
                case .success(let response):
                    // Encode the response to JSON for the Kotlin mapper
                    let encoder = JSONEncoder()
                    let jsonData = try? encoder.encode(response)
                    let jsonString = jsonData != nil ? String(data: jsonData!, encoding: .utf8) : nil
                    
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

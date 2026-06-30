import UIKit
import SwiftUI
import sharedKit

struct ComposeView: UIViewControllerRepresentable {
    let provider: MineSecPaymentProvider

    func makeUIViewController(context: Context) -> UIViewController {
        let bundle = Bundle.main
        let baseUrl = bundle.object(forInfoDictionaryKey: "ConfigBaseURL") as? String
            ?? "https://api-dev.paymentoptions.com/api/v1/"

        // K2 Kotlin/Native only exports baseUrl and mineSecProvider to ObjC/Swift.
        // environment is derived from baseUrl in Kotlin; isDebug defaults to false.
        return MainViewControllerKt.MainViewController(
            baseUrl: baseUrl,
            mineSecProvider: provider
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    let provider: MineSecPaymentProvider

    var body: some View {
        ComposeView(provider: provider)
            .ignoresSafeArea()
    }
}

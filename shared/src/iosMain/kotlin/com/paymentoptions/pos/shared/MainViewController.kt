package com.paymentoptions.pos.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.paymentoptions.pos.App
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.storage.createSettings
import platform.UIKit.UIViewController

/**
 * iOS entry point for the shared Compose Multiplatform UI.
 *
 * Called from AppDelegate / SwiftUI:
 *
 * ```swift
 * import sharedKit
 *
 * struct ContentView: View {
 *     var body: some View {
 *         ComposeView()
 *             .ignoresSafeArea(.all)
 *     }
 * }
 *
 * struct ComposeView: UIViewControllerRepresentable {
 *     func makeUIViewController(context: Context) -> UIViewController {
 *         MainViewControllerKt.MainViewController()
 *     }
 *     func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
 * }
 * ```
 *
 * @param baseUrl  The API base URL, read from Info.plist or injected at build time.
 */
fun MainViewController(
    baseUrl: String = "https://api-dev.paymentoptions.com/api/v1/",
): UIViewController {
    // Initialise storage before the first composition
    AppStorage.init(createSettings())

    return ComposeUIViewController {
        App(buildTimeBaseUrl = baseUrl)
    }
}

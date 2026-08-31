package com.paymentoptions.pos.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.paymentoptions.pos.App
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.storage.createSettings
import com.paymentoptions.pos.device.TapToPayReaderStatusHelper
import com.paymentoptions.pos.device.TapToPayReaderStatusProvider
import com.paymentoptions.pos.payment.MineSecPaymentProvider
import com.paymentoptions.pos.payment.MineSecPlatform
import com.paymentoptions.pos.utils.KeychainHelper
import com.paymentoptions.pos.utils.KeychainProvider
import platform.UIKit.UIViewController

/**
 * iOS entry point for the shared Compose Multiplatform UI.
 *
 * K2 Kotlin/Native only exports the first parameter of each primitive type to ObjC/Swift,
 * so environment and isDebug are handled separately:
 *   - environment is derived from the baseUrl hostname
 *   - isDebug is set via setIsDebugBuild() before calling this function
 *
 * @param baseUrl  The API base URL read from Info.plist at startup.
 * @param mineSecProvider  Implementation of the MineSec SDK logic from Swift.
 * @param keychainProvider  Implementation of Keychain storage from Swift.
 * @param tapToPayReaderStatusProvider  Implementation of the ProximityReader Tap to Pay status check from Swift.
 */
fun MainViewController(
    baseUrl: String,
    mineSecProvider: MineSecPaymentProvider?,
    keychainProvider: KeychainProvider?,
    tapToPayReaderStatusProvider: TapToPayReaderStatusProvider?,
): UIViewController {
    AppStorage.init(createSettings())

    val environment = when {
        baseUrl.contains("api-dev") -> "DEV"
        baseUrl.contains("api-uat") -> "UAT"
        else -> "PROD"
    }

    MineSecPlatform.paymentProvider = mineSecProvider
    KeychainHelper.provider = keychainProvider
    TapToPayReaderStatusHelper.provider = tapToPayReaderStatusProvider

    return ComposeUIViewController {
        App(buildTimeBaseUrl = baseUrl, environment = environment)
    }
}

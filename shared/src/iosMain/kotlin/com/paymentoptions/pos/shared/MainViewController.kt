package com.paymentoptions.pos.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.paymentoptions.pos.App
import com.paymentoptions.pos.setBuildEnvironment
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.storage.createSettings
import com.paymentoptions.pos.device.TapToPayReaderStatusHelper
import com.paymentoptions.pos.device.TapToPayReaderStatusProvider
import com.paymentoptions.pos.payment.MineSecPaymentProvider
import com.paymentoptions.pos.payment.MineSecPlatform
import com.paymentoptions.pos.payment.reader.TapToPayDiscoveryBridge
import com.paymentoptions.pos.payment.reader.TapToPayDiscoveryBridgeHolder
import com.paymentoptions.pos.payment.reader.TapToPayReaderBridge
import com.paymentoptions.pos.payment.reader.TapToPayReaderBridgeHolder
import com.paymentoptions.pos.utils.KeychainHelper
import com.paymentoptions.pos.utils.KeychainProvider
import platform.UIKit.UIViewController
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.ConfigurationManager

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
 * @param tapToPayReaderBridge  Implementation of the Tap to Pay reader lifecycle (ReaderManager) from Swift.
 * @param tapToPayDiscoveryBridge  Implementation of Apple's ProximityReaderDiscovery content presenter from Swift.
 */
fun MainViewController(
    baseUrl: String,
    mineSecProvider: MineSecPaymentProvider?,
    keychainProvider: KeychainProvider?,
    tapToPayReaderStatusProvider: TapToPayReaderStatusProvider?,
    tapToPayReaderBridge: TapToPayReaderBridge?,
    tapToPayDiscoveryBridge: TapToPayDiscoveryBridge?,
): UIViewController {
    AppStorage.init(createSettings())

    // Info.plist gives "" when CONFIG_BASE_URL isn't defined in the xcconfig (ContentView's nil
    // fallback doesn't catch that), so fall back to the same dev URL here.
    val effectiveBaseUrl = if (ConfigurationManager.isUsableBaseUrl(baseUrl)) {
        baseUrl.trim()
    } else {
        AppLogger.warn("ConfigBaseURL from Info.plist is not a usable URL ('$baseUrl'); using ${ConfigurationManager.DEFAULT_BASE_URL}")
        ConfigurationManager.DEFAULT_BASE_URL
    }

    val environment = when {
        effectiveBaseUrl.contains("api-dev") -> "DEV"
        effectiveBaseUrl.contains("api-uat") -> "UAT"
        else -> "PROD"
    }
    setBuildEnvironment(environment, effectiveBaseUrl)

    MineSecPlatform.paymentProvider = mineSecProvider
    KeychainHelper.provider = keychainProvider
    TapToPayReaderStatusHelper.provider = tapToPayReaderStatusProvider
    TapToPayReaderBridgeHolder.bridge = tapToPayReaderBridge
    TapToPayDiscoveryBridgeHolder.bridge = tapToPayDiscoveryBridge

    return ComposeUIViewController {
        App(buildTimeBaseUrl = effectiveBaseUrl, environment = environment)
    }
}

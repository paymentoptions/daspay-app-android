package com.paymentoptions.pos.device

import com.paymentoptions.pos.platformLog
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

/**
 * ProximityReader is a Swift-only framework (no Kotlin/Native cinterop bindings), so the actual
 * `PaymentCardReader.isSupported` / reader-readiness check runs natively in Swift and is injected
 * via [TapToPayReaderStatusHelper] — see `SwiftTapToPayReaderStatusProvider` in iosApp.
 */
actual fun getNfcStatus(): Pair<Boolean, Boolean> {
    val provider = TapToPayReaderStatusHelper.provider
    //return Pair(provider?.isSupported() ?: false, provider?.isReady() ?: false)
    platformLog("TapToPayReaderStatusHelper","ioS is provider ready:" +
            " ${provider?.isReady()}, isSupported: ${provider?.isSupported()}, provider: $provider")
    return Pair(provider?.isSupported() ?: false, provider?.isSupported() ?: false)
}

// iOS has no developer-options toggle equivalent.
actual fun isDeveloperOptionsEnabled(): Boolean = false

actual fun openDevelopmentSettings() {
    val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
    UIApplication.sharedApplication.openURL(url)
}

actual fun openNfcSettings() {
    val deepLink = NSURL.URLWithString("App-Prefs:root=General")
    if (deepLink != null && UIApplication.sharedApplication.canOpenURL(deepLink)) {
        UIApplication.sharedApplication.openURL(deepLink)
    } else {
        val settings = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(settings)
    }
}

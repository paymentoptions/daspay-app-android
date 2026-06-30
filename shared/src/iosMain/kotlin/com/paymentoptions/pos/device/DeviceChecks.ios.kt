package com.paymentoptions.pos.device

import platform.CoreNFC.NFCReaderSession
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

actual fun getNfcStatus(): Pair<Boolean, Boolean> {
    val isSupported = NFCReaderSession.readingAvailable
    return Pair(isSupported, isSupported)
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

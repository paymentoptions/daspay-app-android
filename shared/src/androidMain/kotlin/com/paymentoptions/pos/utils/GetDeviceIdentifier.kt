package com.paymentoptions.pos.utils

import android.provider.Settings
import com.paymentoptions.pos.currentActivity

actual fun getDeviceIdentifier(): String {
    val context = currentActivity ?: return "unknown"
    return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
}

actual fun isAndroid() = true

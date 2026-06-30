package com.paymentoptions.pos.utils

import platform.UIKit.UIDevice

actual fun getDeviceIdentifier(): String {
    val uuid = UIDevice.currentDevice.identifierForVendor?.UUIDString?.replace("-", "") ?: "unknown"
    // Truncate to 16 characters to match Android ID length and avoid potential backend validation errors
    return uuid.take(16)
}

actual fun isAndroid() = false

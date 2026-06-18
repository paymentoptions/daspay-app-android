package com.paymentoptions.pos.utils

import platform.UIKit.UIDevice

actual fun getDeviceIdentifier(): String {
    return UIDevice.currentDevice.identifierForVendor?.UUIDString ?: "unknown"
}

actual fun isAndroid() = false

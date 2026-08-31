@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
package com.paymentoptions.pos.utils

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSUUID
import platform.UIKit.UIDevice

const val DEVICE_KEY = "stableDeviceId"

actual fun getDeviceIdentifier(): String {

    KeychainHelper.get(DEVICE_KEY)?.let {
        return it.take(16)
    }

    val id =
        UIDevice.currentDevice.identifierForVendor?.UUIDString
            ?: NSUUID().UUIDString

    KeychainHelper.put(DEVICE_KEY, value = id)

    return id.take(16)
}

actual fun isAndroid() = false

package com.paymentoptions.pos

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSLog
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import platform.UIKit.UIDevice

@OptIn(ExperimentalForeignApi::class)
// ── Biometrics ────────────────────────────────────────────────────────────────

actual fun getBiometricAuthenticator(): BiometricAuthenticator = object : BiometricAuthenticator {

    override fun isAvailable(): Boolean {
        val ctx = LAContext()
        val error = objcPtr<platform.Foundation.NSError>()
        return ctx.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, error = null)
    }

    override fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val ctx = LAContext()
        ctx.evaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            localizedReason = title
        ) { success, error ->
            if (success) onSuccess()
            else onError(error?.localizedDescription ?: "Biometric failed")
        }
    }
}

// Helper to work around iOS pointer type quirks
private fun <T> objcPtr(): T? = null

// ── FCM ───────────────────────────────────────────────────────────────────────

actual suspend fun getPushToken(): String? = null   // Set up APNs integration here if needed

// ── Device info ───────────────────────────────────────────────────────────────

actual fun getDeviceOs(): String = "iOS"
actual fun getDeviceManufacturer(): String = "Apple"
actual fun getDeviceOsVersion(): String = UIDevice.currentDevice.systemVersion

actual fun getDeviceIpAddress(): String = "0.0.0.0"   // Implement via CFNetwork if needed

// ── Logging ───────────────────────────────────────────────────────────────────

actual fun platformLog(tag: String, message: String) {
    NSLog("$tag: $message")
}

actual fun platformLogError(tag: String, message: String, throwable: Throwable?) {
    NSLog("ERROR $tag: $message ${throwable?.message ?: ""}")
}

// ── Orientation ───────────────────────────────────────────────────────────────

actual fun lockPortrait() {
    // On iOS, orientation locking is handled in AppDelegate / SwiftUI.
    // Leave this as a no-op; configure supported orientations in Xcode project settings.
}

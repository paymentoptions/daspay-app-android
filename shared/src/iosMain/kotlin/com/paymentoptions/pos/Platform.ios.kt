package com.paymentoptions.pos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import coil3.Uri
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.Instant
import platform.Foundation.NSLog
import platform.Foundation.NSURL
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
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

// ── Build flavor ──────────────────────────────────────────────────────────────

actual val isDebugBuild: Boolean = false

actual fun showToast(message: String) {

}

actual fun openNfcSettings() {
    // iOS doesn't have a dedicated NFC toggle in Settings like Android.
    // NFC is always on for supported models. We can try to open General settings or the App's settings.
    val url = NSURL.URLWithString("App-Prefs:root=General")
    if (url != null && UIApplication.sharedApplication.canOpenURL(url)) {
        UIApplication.sharedApplication.openURL(url)
    } else {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (settingsUrl != null) UIApplication.sharedApplication.openURL(settingsUrl)
    }
}

actual fun openDeveloperSettings() {
    // Developer settings only appear if Developer Mode is enabled.
    val url = NSURL.URLWithString("App-Prefs:root=DEVELOPER_SETTINGS")
    if (url != null && UIApplication.sharedApplication.canOpenURL(url)) {
        UIApplication.sharedApplication.openURL(url)
    } else {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (settingsUrl != null) UIApplication.sharedApplication.openURL(settingsUrl)
    }
}

// ── Date Formatting ──────────────────────────────────────────────────────────

actual fun formatDate(instant: Instant, pattern: String): String {
    val date = NSDate.dateWithTimeIntervalSince1970(instant.toEpochMilliseconds() / 1000.0)
    val formatter = NSDateFormatter()
    formatter.dateFormat = pattern
    return formatter.stringFromDate(date)
}

// ── Image Handling ────────────────────────────────────────────────────────────

actual fun createSignatureImage(
    path: Path,
    width: Float,
    height: Int,
    isDrawnTopToBottom: Boolean
): ImageBitmap? {
    return null 
}

actual fun decodeImageFromUri(uri: Any): ImageBitmap? {
    return null
}

actual fun imageBitmapToByteArray(bitmap: ImageBitmap): ByteArray {
    return ByteArray(0)
}

// ── Image Picker ──────────────────────────────────────────────────────────────

@Composable
actual fun rememberKmpImagePickerLauncher(onResult: (Uri?) -> Unit): KmpImagePickerLauncher {
    return remember {
        object : KmpImagePickerLauncher {
            override fun launch() {
                // TODO: Implement native iOS image picking
            }
        }
    }
}

@Composable
actual fun rememberKmpCameraLauncher(onResult: (Uri?) -> Unit): KmpCameraLauncher {
    return remember {
        object : KmpCameraLauncher {
            override fun launch() {
                // TODO: Implement native iOS camera
            }
        }
    }
}

@Composable
actual fun rememberKmpFilePickerLauncher(onResult: (Uri?) -> Unit): KmpFilePickerLauncher {
    return remember {
        object : KmpFilePickerLauncher {
            override fun launch(mimeType: String) {
                // TODO: Implement native iOS file picking
            }
        }
    }
}

// ── Permissions ───────────────────────────────────────────────────────────────

actual fun isLocationPermissionGranted(): Boolean {
    // Basic check for iOS
    return true // Placeholder
}

@Composable
actual fun rememberLocationPermissionLauncher(onResult: (Boolean) -> Unit): KmpPermissionLauncher {
    return remember {
        object : KmpPermissionLauncher {
            override fun launch() {
                onResult(true) // Placeholder
            }
        }
    }
}

// ── Lifecycle ─────────────────────────────────────────────────────────────────

@Composable
actual fun OnResume(onResume: () -> Unit) {
    // Placeholder
}

// ── File Handling ─────────────────────────────────────────────────────────────

actual fun openPdf(filePath: String) {
    // Placeholder
}

actual fun sendLogsToSdkTeam() {
    // Placeholder
}

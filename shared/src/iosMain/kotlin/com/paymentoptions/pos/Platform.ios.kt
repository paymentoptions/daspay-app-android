package com.paymentoptions.pos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import coil3.Uri
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.Instant
import platform.Foundation.NSURL
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIDevice
import com.paymentoptions.pos.logger.AppLogger

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIPopoverArrowDirectionAny

@OptIn(ExperimentalForeignApi::class)
// ── Biometrics ────────────────────────────────────────────────────────────────

actual fun getBiometricAuthenticator(): BiometricAuthenticator = object : BiometricAuthenticator {

    override fun isAvailable(): Boolean {
        val ctx = LAContext()
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

// ── FCM ───────────────────────────────────────────────────────────────────────

actual suspend fun getPushToken(): String? = null   // Set up APNs integration here if needed

// ── Device info ───────────────────────────────────────────────────────────────

actual fun getDeviceOs(): String = "iOS"
actual fun getDeviceManufacturer(): String = "Apple"
actual fun getDeviceOsVersion(): String = UIDevice.currentDevice.systemVersion

actual fun getDeviceIpAddress(): String = "0.0.0.0"   // Implement via CFNetwork if needed

// ── Logging ───────────────────────────────────────────────────────────────────

actual fun platformLog(tag: String, message: String) {
    AppLogger.d(tag, message)
}

actual fun platformLogError(tag: String, message: String, throwable: Throwable?) {
    if (throwable != null) {
        AppLogger.e(tag, "$message - ${throwable.message}", throwable.stackTraceToString())
    } else {
        AppLogger.e(tag, message)
    }
}

// ── Orientation ───────────────────────────────────────────────────────────────

actual fun lockPortrait() {
    // On iOS, orientation locking is handled in AppDelegate / SwiftUI.
}

// ── Build flavor ──────────────────────────────────────────────────────────────

actual val isDebugBuild: Boolean = false

actual fun showToast(message: String) {
    // iOS doesn't have a native toast. Implement via Swift banner if needed.
}

actual fun openNfcSettings() {
    val url = NSURL.URLWithString("App-Prefs:root=General")
    if (url != null && UIApplication.sharedApplication.canOpenURL(url)) {
        UIApplication.sharedApplication.openURL(url)
    } else {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (settingsUrl != null) UIApplication.sharedApplication.openURL(settingsUrl)
    }
}

actual fun openDeveloperSettings() {
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
): ImageBitmap? = null

actual fun decodeImageFromUri(uri: Any): ImageBitmap? = null

actual fun imageBitmapToByteArray(bitmap: ImageBitmap): ByteArray = ByteArray(0)

// ── Image Picker ──────────────────────────────────────────────────────────────

@Composable
actual fun rememberKmpImagePickerLauncher(onResult: (Uri?) -> Unit): KmpImagePickerLauncher {
    return remember {
        object : KmpImagePickerLauncher {
            override fun launch() {}
        }
    }
}

@Composable
actual fun rememberKmpCameraLauncher(onResult: (Uri?) -> Unit): KmpCameraLauncher {
    return remember {
        object : KmpCameraLauncher {
            override fun launch() {}
        }
    }
}

@Composable
actual fun rememberKmpFilePickerLauncher(onResult: (Uri?) -> Unit): KmpFilePickerLauncher {
    return remember {
        object : KmpFilePickerLauncher {
            override fun launch(mimeType: String) {}
        }
    }
}

// ── Permissions ───────────────────────────────────────────────────────────────

actual fun isLocationPermissionGranted(): Boolean = true

@Composable
actual fun rememberLocationPermissionLauncher(onResult: (Boolean) -> Unit): KmpPermissionLauncher {
    return remember {
        object : KmpPermissionLauncher {
            override fun launch() {
                onResult(true)
            }
        }
    }
}

// ── Lifecycle ─────────────────────────────────────────────────────────────────

@Composable
actual fun OnResume(onResume: () -> Unit) {}

// ── File Handling ─────────────────────────────────────────────────────────────

actual fun openPdf(filePath: String) {}

actual fun sendLogsToSdkTeam() {}

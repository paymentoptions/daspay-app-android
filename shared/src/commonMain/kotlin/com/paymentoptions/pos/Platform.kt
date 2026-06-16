package com.paymentoptions.pos

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import coil3.Uri
import kotlinx.datetime.Instant

/**
 * Platform-specific capabilities surfaced to commonMain via expect/actual.
 *
 * Each feature is an interface so callers can inject/mock easily.
 */

// ── Biometric authentication ──────────────────────────────────────────────────

interface BiometricAuthenticator {
    /** Returns true if the device supports and has enrolled biometrics. */
    fun isAvailable(): Boolean

    /**
     * Show the biometric prompt.
     * [onSuccess] is called on the main thread when authentication succeeds.
     * [onError] receives a descriptive message on failure.
     */
    fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )
}

/** Platform-provided [BiometricAuthenticator]. Obtained at runtime. */
expect fun getBiometricAuthenticator(): BiometricAuthenticator

// ── FCM / Push token ──────────────────────────────────────────────────────────

/**
 * Returns the current Firebase/APNs push token, or null on platforms where
 * push is not configured.
 */
expect suspend fun getPushToken(): String?

// ── Device information ────────────────────────────────────────────────────────

expect fun getDeviceOs(): String
expect fun getDeviceManufacturer(): String
expect fun getDeviceOsVersion(): String
expect fun getDeviceIpAddress(): String

// ── Logging ───────────────────────────────────────────────────────────────────

expect fun platformLog(tag: String, message: String)
expect fun platformLogError(tag: String, message: String, throwable: Throwable? = null)

expect fun showToast(message: String)

// ── Screen orientation ────────────────────────────────────────────────────────

/** Lock to portrait; no-op on platforms that don't support it (e.g. iPad). */
expect fun lockPortrait()

// ── Build flavor ──────────────────────────────────────────────────────────────

/**
 * `true` when running a debug build. Wire from the app's `BuildConfig.DEBUG` via
 * [setIsDebugBuild] (Android) at startup. Defaults to `false`.
 */
expect val isDebugBuild: Boolean

// ── URL encoding helpers ──────────────────────────────────────────────────────

/** Percent-encodes [value] for safe use in a URL path/query segment. */
fun urlEncode(value: String): String = buildString {
    for (ch in value) {
        when {
            ch.isLetterOrDigit() || ch in "-._~" -> append(ch)
            else -> ch.toString().encodeToByteArray().forEach { byte ->
                append('%')
                append(byte.toInt().and(0xFF).toString(16).uppercase().padStart(2, '0'))
            }
        }
    }
}

/** Decodes a percent-encoded URL segment. */
fun urlDecode(value: String): String {
    val result = StringBuilder()
    var i = 0
    while (i < value.length) {
        if (value[i] == '%' && i + 2 < value.length) {
            val hex = value.substring(i + 1, i + 3)
            result.append(hex.toInt(16).toChar())
            i += 3
        } else if (value[i] == '+') {
            result.append(' ')
            i++
        } else {
            result.append(value[i])
            i++
        }
    }
    return result.toString()
}

// ── Settings Navigation ───────────────────────────────────────────────────────

/** Open device NFC settings. No-op if not supported. */
expect fun openNfcSettings()

/** Open device Developer Options. No-op if not supported. */
expect fun openDeveloperSettings()

// ── Date Formatting ──────────────────────────────────────────────────────────

/**
 * Formats a timestamp (epoch milliseconds) using the given pattern.
 * Uses platform-native formatters (SimpleDateFormat on Android, NSDateFormatter on iOS).
 */
expect fun formatDate(instant: Instant, pattern: String): String

// ── Image Handling ────────────────────────────────────────────────────────────

/** Generates a signature image from a Path. */
expect fun createSignatureImage(
    path: Path,
    width: Float,
    height: Int,
    isDrawnTopToBottom: Boolean
): ImageBitmap?

/** Converts an ImageBitmap to a ByteArray (PNG format). */
expect fun imageBitmapToByteArray(bitmap: ImageBitmap): ByteArray

/** Decodes an image from a URI (represented as Any for platform flexibility). */
expect fun decodeImageFromUri(uri: Any): ImageBitmap?

// ── Image Picker ──────────────────────────────────────────────────────────────

interface KmpImagePickerLauncher {
    fun launch()
}

/** Remembers a platform-specific image picker launcher. */
@Composable
expect fun rememberKmpImagePickerLauncher(onResult: (Uri?) -> Unit): KmpImagePickerLauncher

interface KmpCameraLauncher {
    fun launch()
}

/** Remembers a platform-specific camera launcher. */
@Composable
expect fun rememberKmpCameraLauncher(onResult: (Uri?) -> Unit): KmpCameraLauncher

interface KmpFilePickerLauncher {
    fun launch(mimeType: String)
}

/** Remembers a platform-specific file picker launcher. */
@Composable
expect fun rememberKmpFilePickerLauncher(onResult: (Uri?) -> Unit): KmpFilePickerLauncher

// ── Permissions ───────────────────────────────────────────────────────────────

interface KmpPermissionLauncher {
    fun launch()
}

/** Remembers a launcher to request location permission. */
@Composable
expect fun rememberLocationPermissionLauncher(onResult: (Boolean) -> Unit): KmpPermissionLauncher

/** Returns true if location permission is already granted. */
expect fun isLocationPermissionGranted(): Boolean

// ── Lifecycle ─────────────────────────────────────────────────────────────────

/** Performs [onResume] whenever the screen becomes active/resumed. */
@Composable
expect fun OnResume(onResume: () -> Unit)

// ── File Handling ─────────────────────────────────────────────────────────────

/** Opens a PDF file using the platform's default viewer. */
expect fun openPdf(filePath: String)

/** Zips and shares application logs via email/chooser. */
expect fun sendLogsToSdkTeam()

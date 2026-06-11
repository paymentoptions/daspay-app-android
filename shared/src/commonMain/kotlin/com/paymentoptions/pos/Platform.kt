package com.paymentoptions.pos

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

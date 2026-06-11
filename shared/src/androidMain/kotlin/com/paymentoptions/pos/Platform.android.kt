package com.paymentoptions.pos

import android.content.pm.ActivityInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// ── Activity reference ────────────────────────────────────────────────────────
// MainActivity sets this during onCreate so platform functions can use it.
var currentActivity: FragmentActivity? = null

// ── Biometrics ────────────────────────────────────────────────────────────────

actual fun getBiometricAuthenticator(): BiometricAuthenticator = object : BiometricAuthenticator {

    override fun isAvailable(): Boolean {
        val activity = currentActivity ?: return false
        val manager = BiometricManager.from(activity)
        return manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    override fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val activity = currentActivity ?: run {
            onError("No active activity")
            return
        }
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errString.toString())
                }
                override fun onAuthenticationFailed() {
                    onError("Authentication failed")
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancel")
            .build()
        prompt.authenticate(info)
    }
}

// ── FCM ───────────────────────────────────────────────────────────────────────

actual suspend fun getPushToken(): String? = suspendCancellableCoroutine { cont ->
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (task.isSuccessful) cont.resume(task.result)
        else cont.resume(null)
    }
}

// ── Device info ───────────────────────────────────────────────────────────────

actual fun getDeviceOs(): String = "Android"
actual fun getDeviceManufacturer(): String = Build.MANUFACTURER
actual fun getDeviceOsVersion(): String = Build.VERSION.RELEASE

actual fun getDeviceIpAddress(): String {
    return try {
        val context = currentActivity ?: return "0.0.0.0"
        val wm = context.getSystemService(android.content.Context.WIFI_SERVICE) as? WifiManager
        val ip = wm?.connectionInfo?.ipAddress ?: 0
        "${ip and 0xff}.${ip shr 8 and 0xff}.${ip shr 16 and 0xff}.${ip shr 24 and 0xff}"
    } catch (e: Exception) {
        "0.0.0.0"
    }
}

// ── Logging ───────────────────────────────────────────────────────────────────

actual fun platformLog(tag: String, message: String) {
    Log.d(tag, message)
}

actual fun platformLogError(tag: String, message: String, throwable: Throwable?) {
    Log.e(tag, message, throwable)
}

actual fun showToast(message: String) {
    currentActivity?.let {
        Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
    }
}

// ── Orientation ───────────────────────────────────────────────────────────────

actual fun lockPortrait() {
    currentActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}

// ── Build flavor ──────────────────────────────────────────────────────────────

@Volatile
private var isDebugBuildHolder: Boolean = false

actual val isDebugBuild: Boolean
    get() = isDebugBuildHolder

/** Wire from app `BuildConfig.DEBUG` at startup. */
fun setIsDebugBuild(value: Boolean) {
    isDebugBuildHolder = value
}

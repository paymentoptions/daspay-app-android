package com.paymentoptions.pos

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri as AndroidUri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.Uri
import coil3.toCoilUri
import com.google.firebase.messaging.FirebaseMessaging
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.logger.ExportLogs
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.Instant
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.roundToInt

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
    AppLogger.debug(tag, message)
}

actual fun platformLogError(tag: String, message: String, throwable: Throwable?) {
    AppLogger.error(tag, message)
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

// ── Settings Navigation ───────────────────────────────────────────────────────

actual fun openNfcSettings() {
    currentActivity?.let { activity ->
        try {
            activity.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        } catch (e: Exception) {
            platformLogError("Platform", "Failed to open NFC settings", e)
        }
    }
}

actual fun openDeveloperSettings() {
    currentActivity?.let { activity ->
        try {
            activity.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        } catch (e: Exception) {
            platformLogError("Platform", "Failed to open Developer settings", e)
        }
    }
}

// ── Date Formatting ──────────────────────────────────────────────────────────

actual fun formatDate(instant: Instant, pattern: String): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(instant.toEpochMilliseconds()))
}

// ── Image Handling ────────────────────────────────────────────────────────────

actual fun createSignatureImage(
    path: Path,
    width: Float,
    height: Int,
    isDrawnTopToBottom: Boolean
): ImageBitmap? {
    val bounds = path.getBounds()

    val croppedBitmap = createBitmap(
        bounds.width.roundToInt(),
        bounds.height.roundToInt()
    )
    val canvas = android.graphics.Canvas(croppedBitmap)
    canvas.drawColor(android.graphics.Color.TRANSPARENT)

    val croppedPath = Path().apply { addPath(path) }
    croppedPath.translate(Offset(-bounds.left, -bounds.top))

    val paint = Paint().apply {
        color = android.graphics.Color.BLACK
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 8f
        isAntiAlias = true
    }
    canvas.drawPath(croppedPath.asAndroidPath(), paint)

    // aspect ratio with threshold to determine if signature is vertical
    val aspectRatio = bounds.height / bounds.width

    // If signature is significantly taller than wide threshold of 1.2 or higher
    if (aspectRatio > 1.2f) {
        val signatureCenterX = bounds.left + (bounds.width / 2f)
        val canvasCenterX = width / 2f

        val matrix = Matrix().apply {
            if (signatureCenterX < canvasCenterX) {
                postRotate(270f)
            } else {
                postRotate(90f)
            }
        }

        return Bitmap.createBitmap(
            croppedBitmap, 0, 0, croppedBitmap.width, croppedBitmap.height, matrix, true
        ).asImageBitmap()
    }

    return croppedBitmap.asImageBitmap()
}

actual fun imageBitmapToByteArray(bitmap: ImageBitmap): ByteArray {
    val androidBitmap = bitmap.asAndroidBitmap()
    val output = ByteArrayOutputStream()
    androidBitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    return output.toByteArray()
}

actual fun decodeImageFromUri(uri: Any): ImageBitmap? {
    val androidUri = uri as? AndroidUri ?: (uri as? Uri)?.let { AndroidUri.parse(it.toString()) } ?: return null
    return currentActivity?.contentResolver?.openInputStream(androidUri)?.use {
        BitmapFactory.decodeStream(it)?.asImageBitmap()
    }
}

// ── Image Picker ──────────────────────────────────────────────────────────────

@Composable
actual fun rememberKmpImagePickerLauncher(onResult: (Uri?) -> Unit): KmpImagePickerLauncher {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: AndroidUri? ->
        onResult(uri?.toCoilUri())
    }
    return remember {
        object : KmpImagePickerLauncher {
            override fun launch() {
                launcher.launch("image/*")
            }
        }
    }
}

@Composable
actual fun rememberKmpCameraLauncher(onResult: (Uri?) -> Unit): KmpCameraLauncher {
    val context = currentActivity ?: throw IllegalStateException("Activity not found")
    var tempCameraUri by remember { mutableStateOf<AndroidUri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            onResult(tempCameraUri?.toCoilUri())
        } else {
            onResult(null)
        }
    }

    return remember {
        object : KmpCameraLauncher {
            override fun launch() {
                val cameraPermission = Manifest.permission.CAMERA
                if (ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
                    val tempFile = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
                    tempCameraUri = uri
                    launcher.launch(uri)
                } else {
                    ActivityCompat.requestPermissions(context, arrayOf(cameraPermission), 1001)
                }
            }
        }
    }
}

@Composable
actual fun rememberKmpFilePickerLauncher(onResult: (Uri?) -> Unit): KmpFilePickerLauncher {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: AndroidUri? ->
        onResult(uri?.toCoilUri())
    }
    return remember {
        object : KmpFilePickerLauncher {
            override fun launch(mimeType: String) {
                launcher.launch(mimeType)
            }
        }
    }
}

// ── Permissions ───────────────────────────────────────────────────────────────

actual fun isLocationPermissionGranted(): Boolean {
    val context = currentActivity ?: return false
    return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

@Composable
actual fun rememberLocationPermissionLauncher(onResult: (Boolean) -> Unit): KmpPermissionLauncher {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onResult(fineGranted || coarseGranted)
    }
    return remember {
        object : KmpPermissionLauncher {
            override fun launch() {
                launcher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
}

// ── Lifecycle ─────────────────────────────────────────────────────────────────

@Composable
actual fun OnResume(onResume: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

// ── File Handling ─────────────────────────────────────────────────────────────

actual fun openPdf(filePath: String) {
    currentActivity?.let { activity ->
        try {
            val file = File(filePath)
            val uri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            platformLogError("Platform", "Failed to open PDF", e)
        }
    }
}

actual fun sendLogsToSdkTeam() {
    currentActivity?.let { activity ->
        ExportLogs.sendLogsToSdkTeam(activity)
    }
}

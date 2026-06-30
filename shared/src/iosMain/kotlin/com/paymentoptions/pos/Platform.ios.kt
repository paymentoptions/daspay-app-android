package com.paymentoptions.pos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.asSkiaPath
import coil3.Uri
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.readBytes
import kotlinx.datetime.Instant
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PaintStrokeCap
import org.jetbrains.skia.PaintStrokeJoin
import kotlin.concurrent.Volatile
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNumber
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.UIKit.UIDevice
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time
import com.paymentoptions.pos.logger.AppLogger
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UILabel
import platform.UIKit.UIView
import kotlin.math.roundToInt

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
            dispatch_async(dispatch_get_main_queue()) {
                if (success) onSuccess()
                else onError(error?.localizedDescription ?: "Biometric failed")
            }
        }
    }
}

// ── FCM / APNs ────────────────────────────────────────────────────────────────
// Swift side calls setPushToken() after registering with APNs.

@Volatile
private var storedPushToken: String? = null

fun setPushToken(token: String) {
    storedPushToken = token
}

actual suspend fun getPushToken(): String? = storedPushToken

// ── Device info ───────────────────────────────────────────────────────────────

actual fun getDeviceOs(): String = "iOS"
actual fun getDeviceManufacturer(): String = "Apple"
actual fun getDeviceOsVersion(): String = UIDevice.currentDevice.systemVersion

// Swift should call setDeviceIpAddress() on startup (e.g. via CFNetwork/getifaddrs in Swift).
@Volatile
private var storedDeviceIpAddress: String = "0.0.0.0"

fun setDeviceIpAddress(ip: String) {
    storedDeviceIpAddress = ip
}

actual fun getDeviceIpAddress(): String = storedDeviceIpAddress

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
    dispatch_async(dispatch_get_main_queue()) {
        // UIDeviceOrientationPortrait = 1
        // UIDeviceOrientationPortrait = 1
        // Orientation lock on iOS is handled via AppDelegate; KVC approach is a best-effort hint.
    }
}

// ── Build flavor ──────────────────────────────────────────────────────────────

@Volatile
private var isDebugBuildHolder: Boolean = false

actual val isDebugBuild: Boolean
    get() = isDebugBuildHolder

fun setIsDebugBuild(value: Boolean) {
    isDebugBuildHolder = value
}

@OptIn(ExperimentalForeignApi::class)
actual fun showToast(message: String) {
    dispatch_async(dispatch_get_main_queue()) {
        val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController
            ?: return@dispatch_async
        var presenter = rootController
        while (presenter.presentedViewController != null) {
            presenter = presenter.presentedViewController!!
        }

        val alert = UIAlertController.alertControllerWithTitle(
            title = null,
            message = message,
            preferredStyle = UIAlertControllerStyleAlert,
        )
        presenter.presentViewController(alert, animated = true, completion = null)

        val delay = dispatch_time(DISPATCH_TIME_NOW, 2_000_000_000L)
        dispatch_after(delay, dispatch_get_main_queue()) {
            alert.dismissViewControllerAnimated(true, completion = null)
        }
    }
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
    isDrawnTopToBottom: Boolean,
): ImageBitmap? {
    val bounds = path.getBounds()
    if (bounds.isEmpty) return null

    val bitmapWidth = bounds.width.roundToInt().coerceAtLeast(1)
    val bitmapHeight = bounds.height.roundToInt().coerceAtLeast(1)

    val bitmap = Bitmap()
    bitmap.allocN32Pixels(bitmapWidth, bitmapHeight)
    val canvas = Canvas(bitmap)
    canvas.clear(org.jetbrains.skia.Color.TRANSPARENT)

    val paint = Paint().apply {
        color = org.jetbrains.skia.Color.BLACK
        mode = PaintMode.STROKE
        strokeWidth = 8f
        strokeCap = PaintStrokeCap.ROUND
        strokeJoin = PaintStrokeJoin.ROUND
        isAntiAlias = true
    }

    canvas.save()
    canvas.translate(-bounds.left, -bounds.top)
    canvas.drawPath(path.asSkiaPath(), paint)
    canvas.restore()

    return bitmap.asComposeImageBitmap()
}

@OptIn(ExperimentalForeignApi::class)
actual fun decodeImageFromUri(uri: Any): ImageBitmap? {
    return try {
        val path = uri.toString().removePrefix("file://").substringBefore("?")
        memScoped {
            val file = platform.posix.fopen(path, "rb") ?: return null
            platform.posix.fseek(file, 0, platform.posix.SEEK_END)
            val size = platform.posix.ftell(file).toInt()
            if (size <= 0) { platform.posix.fclose(file); return null }
            platform.posix.fseek(file, 0, platform.posix.SEEK_SET)
            val buf = allocArray<ByteVar>(size)
            platform.posix.fread(buf, 1u.convert(), size.convert(), file)
            platform.posix.fclose(file)
            val bytes = buf.readBytes(size)
            val skiaImage = Image.makeFromEncoded(bytes)
            Bitmap.makeFromImage(skiaImage).asComposeImageBitmap()
        }
    } catch (e: Exception) {
        null
    }
}

actual fun imageBitmapToByteArray(bitmap: ImageBitmap): ByteArray {
    return Image.makeFromBitmap(bitmap.asSkiaBitmap())
        .encodeToData(EncodedImageFormat.PNG, 100)
        ?.bytes
        ?: ByteArray(0)
}

// ── Image Picker ──────────────────────────────────────────────────────────────
// UIImagePickerController source-type constants are not in the commonized klib.
// Swift layer should bridge these via a KMP expect or a @ObjCName-annotated entry point.
// For now the launchers are no-ops; Swift can call the Kotlin callbacks directly.

@Composable
actual fun rememberKmpImagePickerLauncher(onResult: (Uri?) -> Unit): KmpImagePickerLauncher {
    return remember { object : KmpImagePickerLauncher { override fun launch() {} } }
}

@Composable
actual fun rememberKmpCameraLauncher(onResult: (Uri?) -> Unit): KmpCameraLauncher {
    return remember { object : KmpCameraLauncher { override fun launch() {} } }
}

@Composable
actual fun rememberKmpFilePickerLauncher(onResult: (Uri?) -> Unit): KmpFilePickerLauncher {
    return remember { object : KmpFilePickerLauncher { override fun launch(mimeType: String) {} } }
}

// ── Permissions ───────────────────────────────────────────────────────────────

actual fun isLocationPermissionGranted(): Boolean {
    val status = platform.CoreLocation.CLLocationManager.authorizationStatus()
    return status == platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways ||
            status == platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
}

// Both held strongly — CLLocationManager.delegate is a weak ObjC property, so if the
// manager is released by ARC the delegate never fires. Keep both alive until the user responds.
private var locationDelegate: platform.darwin.NSObject? = null
private var locationManager: platform.CoreLocation.CLLocationManager? = null

@Composable
actual fun rememberLocationPermissionLauncher(onResult: (Boolean) -> Unit): KmpPermissionLauncher {
    val callbackRef = rememberUpdatedState(onResult)
    return remember {
        object : KmpPermissionLauncher {
            override fun launch() {
                val status = platform.CoreLocation.CLLocationManager.authorizationStatus()
                if (status != platform.CoreLocation.kCLAuthorizationStatusNotDetermined) {
                    // Already decided in a previous session — report immediately.
                    callbackRef.value(isLocationPermissionGranted())
                    return
                }

                // First-time request: keep both manager AND delegate alive until the user responds.
                val mgr = platform.CoreLocation.CLLocationManager()
                val delegate = object : platform.darwin.NSObject(),
                    platform.CoreLocation.CLLocationManagerDelegateProtocol {

                    // iOS 14+ callback
                    override fun locationManagerDidChangeAuthorization(
                        manager: platform.CoreLocation.CLLocationManager,
                    ) {
                        val newStatus = platform.CoreLocation.CLLocationManager.authorizationStatus()
                        if (newStatus == platform.CoreLocation.kCLAuthorizationStatusNotDetermined) return
                        val granted = newStatus == platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways ||
                                newStatus == platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
                        dispatch_async(dispatch_get_main_queue()) { callbackRef.value(granted) }
                        locationManager = null
                        locationDelegate = null
                    }

                    // iOS < 14 fallback
                    override fun locationManager(
                        manager: platform.CoreLocation.CLLocationManager,
                        didChangeAuthorizationStatus: platform.CoreLocation.CLAuthorizationStatus,
                    ) {
                        if (didChangeAuthorizationStatus == platform.CoreLocation.kCLAuthorizationStatusNotDetermined) return
                        val granted = didChangeAuthorizationStatus == platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways ||
                                didChangeAuthorizationStatus == platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
                        dispatch_async(dispatch_get_main_queue()) { callbackRef.value(granted) }
                        locationManager = null
                        locationDelegate = null
                    }
                }
                locationManager = mgr   // prevent ARC from releasing the manager
                locationDelegate = delegate
                mgr.delegate = delegate
                mgr.requestWhenInUseAuthorization()
            }
        }
    }
}

// ── Lifecycle ─────────────────────────────────────────────────────────────────

@Composable
actual fun OnResume(onResume: () -> Unit) {
    val callbackRef = androidx.compose.runtime.rememberUpdatedState(onResume)
    androidx.compose.runtime.DisposableEffect(Unit) {
        val observer = platform.Foundation.NSNotificationCenter.defaultCenter.addObserverForName(
            name = platform.UIKit.UIApplicationWillEnterForegroundNotification,
            `object` = null,
            queue = platform.Foundation.NSOperationQueue.mainQueue,
        ) { _ -> callbackRef.value() }
        onDispose {
            platform.Foundation.NSNotificationCenter.defaultCenter.removeObserver(observer)
        }
    }
}

// ── File Handling ─────────────────────────────────────────────────────────────

actual fun openPdf(filePath: String) {
    dispatch_async(dispatch_get_main_queue()) {
        val url = NSURL.fileURLWithPath(filePath)
        val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return@dispatch_async
        var topVC = rootVC
        while (topVC.presentedViewController != null) topVC = topVC.presentedViewController!!
        val activityVC = platform.UIKit.UIActivityViewController(
            activityItems = listOf(url),
            applicationActivities = null,
        )
        topVC.presentViewController(activityVC, animated = true, completion = null)
    }
}

actual fun sendLogsToSdkTeam() {
    dispatch_async(dispatch_get_main_queue()) {
        val logUrl = AppLogger.getLogFileUrl() ?: return@dispatch_async
        val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return@dispatch_async
        var topVC = rootVC
        while (topVC.presentedViewController != null) topVC = topVC.presentedViewController!!
        val activityVC = platform.UIKit.UIActivityViewController(
            activityItems = listOf(logUrl),
            applicationActivities = null,
        )
        topVC.presentViewController(activityVC, animated = true, completion = null)
    }
}

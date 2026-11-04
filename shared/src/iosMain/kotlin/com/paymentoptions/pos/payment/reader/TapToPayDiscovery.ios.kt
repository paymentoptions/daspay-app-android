package com.paymentoptions.pos.payment.reader

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Implemented in Swift by `ProximityReaderDiscoveryPresenter`, wrapping Apple's
 * `ProximityReaderDiscovery` (iOS 18+ only — the app's deployment target is 18.5, so no fallback
 * is needed). Callback-based, matching the existing [TapToPayReaderBridge] pattern.
 */
interface TapToPayDiscoveryBridge {
    fun presentHowToTap(completion: (success: Boolean, errorMessage: String?) -> Unit)
}

/** Holds the Swift-provided [TapToPayDiscoveryBridge], set once from `MainViewController`. */
object TapToPayDiscoveryBridgeHolder {
    var bridge: TapToPayDiscoveryBridge? = null
}

actual fun isTapToPayEducationAvailable(): Boolean = TapToPayDiscoveryBridgeHolder.bridge != null

actual suspend fun showTapToPayEducation() {
    val bridge = TapToPayDiscoveryBridgeHolder.bridge ?: return
    suspendCancellableCoroutine<Unit> { cont ->
        bridge.presentHowToTap { _, _ ->
            cont.resume(Unit)
        }
    }
}

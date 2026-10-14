package com.paymentoptions.pos.device

import kotlin.concurrent.Volatile

/**
 * Singleton to hold the Tap to Pay reader status provider instance injected from the iOS App.
 */
object TapToPayReaderStatusHelper {
    @Volatile
    var provider: TapToPayReaderStatusProvider? = null
}

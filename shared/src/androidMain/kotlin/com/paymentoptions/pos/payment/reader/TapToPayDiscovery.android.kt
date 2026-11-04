package com.paymentoptions.pos.payment.reader

/** No Android equivalent to Apple's `ProximityReaderDiscovery` exists — hidden/no-op. */
actual fun isTapToPayEducationAvailable(): Boolean = false

actual suspend fun showTapToPayEducation() {
    // No-op — callers gate on isTapToPayEducationAvailable() and shouldn't reach this.
}

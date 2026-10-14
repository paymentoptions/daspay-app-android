package com.paymentoptions.pos.device

/**
 * Bridges Apple's ProximityReader Tap to Pay reader status to the native iOS implementation.
 * ProximityReader is a Swift-only framework with no Kotlin/Native cinterop bindings, so the
 * check is implemented natively in Swift and injected here, same pattern as [com.paymentoptions.pos.utils.KeychainProvider].
 */
interface TapToPayReaderStatusProvider {
    /** Device/OS eligibility — backed by `PaymentCardReader.isSupported`. */
    fun isSupported(): Boolean

    /** Runtime readiness — backed by observing `PaymentCardReader.events` for `.readyForTap`/`.notReady`. */
    fun isReady(): Boolean
}

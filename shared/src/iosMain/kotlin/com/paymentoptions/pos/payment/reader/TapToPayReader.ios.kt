package com.paymentoptions.pos.payment.reader

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Native-side reader lifecycle, implemented in Swift by `ReaderManagerBridge` (wrapping the
 * `ReaderManager` actor). Primitive-only, callback-based methods — matching the existing
 * [com.paymentoptions.pos.payment.MineSecPaymentProvider] pattern — since Swift classes
 * implementing a Kotlin-defined, ObjC-exported protocol can't reliably conform to Kotlin
 * `suspend fun`s.
 */
interface TapToPayReaderBridge {
    fun start(
        profileId: String,
        merchantBusinessName: String,
        merchantId: String,
        merchantCategoryCode: String,
        terminalEmvProfileId: String,
    )

    fun stop()
    fun onForeground()

    /** `kind` is one of: notInitialized, initializing, configuring, notLinked, ready, failed. */
    fun ensureReadyForPayment(completion: (kind: String, progress: Int, message: String?) -> Unit)

    fun setStateListener(listener: TapToPayReaderBridgeStateListener)
}

/** Implemented on the Kotlin side; Swift calls this whenever the reader's state changes. */
interface TapToPayReaderBridgeStateListener {
    fun onStateChanged(kind: String, progress: Int, message: String?)
}

/** Holds the Swift-provided [TapToPayReaderBridge], set once from `MainViewController`. */
object TapToPayReaderBridgeHolder {
    var bridge: TapToPayReaderBridge? = null
}

object IosTapToPayReader : TapToPayReader, TapToPayReaderBridgeStateListener {
    private val _state = MutableStateFlow<ReaderState>(ReaderState.NotInitialized)
    override val state: StateFlow<ReaderState> = _state

    private var listenerAttached = false

    private fun bridge(): TapToPayReaderBridge? {
        val current = TapToPayReaderBridgeHolder.bridge
        if (current != null && !listenerAttached) {
            current.setStateListener(this)
            listenerAttached = true
        }
        return current
    }

    override fun start(config: MerchantConfig) {
        bridge()?.start(
            profileId = config.profileId,
            merchantBusinessName = config.merchantBusinessName,
            merchantId = config.merchantId,
            merchantCategoryCode = config.merchantCategoryCode,
            terminalEmvProfileId = config.terminalEmvProfileId,
        )
    }

    override fun stop() {
        bridge()?.stop()
    }

    override fun onForeground() {
        bridge()?.onForeground()
    }

    override suspend fun ensureReadyForPayment(): ReaderState = suspendCancellableCoroutine { cont ->
        val current = bridge()
        if (current == null) {
            cont.resume(ReaderState.Failed("Tap to Pay reader is not available."))
        } else {
            current.ensureReadyForPayment { kind, progress, message ->
                cont.resume(toReaderState(kind, progress, message))
            }
        }
    }

    override fun onStateChanged(kind: String, progress: Int, message: String?) {
        _state.value = toReaderState(kind, progress, message)
    }

    private fun toReaderState(kind: String, progress: Int, message: String?): ReaderState = when (kind) {
        "notInitialized" -> ReaderState.NotInitialized
        "initializing" -> ReaderState.Initializing
        "configuring" -> ReaderState.Configuring(progress)
        "notLinked" -> ReaderState.NotLinked
        "ready" -> ReaderState.Ready
        else -> ReaderState.Failed(message ?: "Unknown reader state: $kind")
    }
}

actual fun getTapToPayReader(): TapToPayReader = IosTapToPayReader

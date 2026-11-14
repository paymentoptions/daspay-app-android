package com.paymentoptions.pos.payment.reader

import kotlinx.coroutines.flow.StateFlow

/** Lifecycle state of the platform Tap to Pay reader. */
sealed class ReaderState {
    data object NotInitialized : ReaderState()
    data object Initializing : ReaderState()
    data class Configuring(val progress: Int) : ReaderState()
    data object NotLinked : ReaderState()
    data object Ready : ReaderState()
    data class Failed(val message: String) : ReaderState()
}

/**
 * Single cross-platform entry point for the Tap to Pay reader lifecycle.
 * Started once by [ReaderLifecycleCoordinator] — never from a screen or ViewModel.
 */
interface TapToPayReader {
    val state: StateFlow<ReaderState>

    /** No-op if already started with an identical [config]; otherwise restarts with the new one. */
    fun start(config: MerchantConfig)

    fun stop()

    /** Call when the app returns to the foreground; warms the reader up if already started. */
    fun onForeground()

    /** Suspends until the reader is ready for a tap (or fails), returning the resulting state. */
    suspend fun ensureReadyForPayment(): ReaderState
    /**
     * Drives Apple's Tap to Pay on iPhone account-linking (Terms & Conditions) flow. No-op on
     * platforms with no such concept (Android), which just resolve immediately. Always returns
     * the resulting [ReaderState] — [ReaderState.Failed] carries a clear, case-specific message
     * when linking is required/declined/fails.
     */
    suspend fun linkAccount(): ReaderState
}

/** Platform-provided [TapToPayReader] singleton. */
expect fun getTapToPayReader(): TapToPayReader

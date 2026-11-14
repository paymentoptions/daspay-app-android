package com.paymentoptions.pos.payment.reader

import com.paymentoptions.pos.device.getNfcStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Android's MineSec SDK (`com.theminesec.sdk:headless`) has no app-visible reader-lifecycle,
 * merchant-identity, or account-link API: `HeadlessSetup.initSoftPos(context, licenseFile)` takes
 * no merchant config at all, and readiness/warm-up live entirely inside the SDK's own
 * `HeadlessActivity` per transaction (confirmed by decompiling headless-1.2.17-live-release.aar —
 * no `linkAccount`/`warmUp`/`accountStatus`/`readiness`/`getReaderStatus` method exists anywhere
 * in the AAR). There is therefore nothing to start/stop/warm up on this platform; this just
 * wraps the existing NFC-adapter check behind the shared [TapToPayReader] contract.
 */
object AndroidTapToPayReader : TapToPayReader {
    private val _state = MutableStateFlow<ReaderState>(ReaderState.NotInitialized)
    override val state: StateFlow<ReaderState> = _state

    override fun start(config: MerchantConfig) {
        _state.value = currentNfcState()
    }

    override fun stop() {
        _state.value = ReaderState.NotInitialized
    }

    override fun onForeground() {
        _state.value = currentNfcState()
    }

    override suspend fun ensureReadyForPayment(): ReaderState {
        val result = currentNfcState()
        _state.value = result
        return result
    }

    /** Android has no account-linking concept (see class doc) — just re-checks NFC readiness. */
    override suspend fun linkAccount(): ReaderState = ensureReadyForPayment()

    private fun currentNfcState(): ReaderState {
        val (isSupported, isEnabled) = getNfcStatus()
        return when {
            !isSupported -> ReaderState.Failed("NFC is not supported on this device.")
            !isEnabled -> ReaderState.Failed("NFC is turned off. Enable it in Settings.")
            else -> ReaderState.Ready
        }
    }
}

actual fun getTapToPayReader(): TapToPayReader = AndroidTapToPayReader

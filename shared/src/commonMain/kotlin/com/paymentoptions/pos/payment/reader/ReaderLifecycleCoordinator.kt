package com.paymentoptions.pos.payment.reader

import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.storage.AppStorage
import kotlin.concurrent.Volatile

/**
 * App-scoped owner of the [TapToPayReader] lifecycle. Started once from the app root
 * ([com.paymentoptions.pos.App]) — never from a screen or ViewModel, which only report
 * lifecycle transitions ([sessionEstablished]/[sessionEnded]) through this object.
 */
object ReaderLifecycleCoordinator {
    private val reader: TapToPayReader by lazy { getTapToPayReader() }

    @Volatile private var loggedIn = false
    @Volatile private var startedConfig: MerchantConfig? = null

    /** Call once at app startup; resumes the reader if a session was already active. */
    fun appStarted() {
        loggedIn = AppStorage.isLoggedIn()
        val config = DPStorageManager.getMerchantConfig()
        if (loggedIn && config != null) {
            startReader(config)
        }
    }

    /** Call when a login completes or a session is restored and a [MerchantConfig] becomes available. */
    fun sessionEstablished(config: MerchantConfig) {
        loggedIn = true
        startReader(config)
    }

    /** Call on sign-out / session invalidation. */
    fun sessionEnded() {
        loggedIn = false
        startedConfig = null
        reader.stop()
    }

    /** Call when the app returns to the foreground. */
    fun onForeground() {
        if (!loggedIn) return
        val config = startedConfig ?: DPStorageManager.getMerchantConfig() ?: return
        if (startedConfig == null) {
            startReader(config)
        } else {
            reader.onForeground()
        }
    }

    private fun startReader(config: MerchantConfig) {
        if (startedConfig == config) return
        startedConfig = config
        reader.start(config)
    }
}

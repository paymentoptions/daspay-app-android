package com.paymentoptions.pos.services.apiService

import com.paymentoptions.pos.auth.TokenRepository
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.storage.AppStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile

/**
 * Sign-in / sign-out hooks.
 * Token refresh itself is handled by [com.paymentoptions.pos.auth.TokenRepository] (shared module).
 */
class TokenAutoRefresher private constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Call once sign-in has saved the tokens: schedules token refresh and downloads the app config. */
    fun onUserSignedIn() {
        val expiryEpochMs = AppStorage.tokenExpiry
        if (expiryEpochMs > 0L) {
            TokenRepository.scheduleProactiveRefresh(expiryEpochMs = expiryEpochMs)
        }
        scope.launch {
            try {
                if (ConfigurationManager.initializeConfig()) {
                    AppLogger.info("Config initialized successfully after sign in")
                } else {
                    AppLogger.warn("Config initialization failed after sign in, using default base URL")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLogger.error("Error initializing config after sign in: ${e.message}")
            }
        }
    }

    fun onUserSignedOut() {
        TokenRepository.stop()
    }

    companion object {
        @Volatile private var INSTANCE: TokenAutoRefresher = TokenAutoRefresher()

        fun getInstance() = INSTANCE
    }
}

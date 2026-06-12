package com.paymentoptions.pos.services.apiService

import com.paymentoptions.pos.auth.TokenRepository
import com.paymentoptions.pos.storage.AppStorage
import kotlin.concurrent.Volatile

/**
 * No-op stub kept for call-site compatibility.
 * Token refresh is now handled by [com.paymentoptions.pos.auth.TokenRepository] (shared module).
 */
class TokenAutoRefresher private constructor() {

    fun onUserSignedIn() {
        val expiryEpochMs = AppStorage.tokenExpiry
        if (expiryEpochMs > 0L) {
            TokenRepository.scheduleProactiveRefresh(expiryEpochMs = expiryEpochMs)
        }
    }

    fun onUserSignedOut() {
        TokenRepository.stop()
    }

    companion object {
        @Volatile private var INSTANCE: TokenAutoRefresher = TokenAutoRefresher()

        fun getInstance() = INSTANCE

//        private fun instantiate(): () -> TokenAutoRefresher = {
//            INSTANCE ?: TokenAutoRefresher().also { INSTANCE = it }
//        }
    }
}

package com.paymentoptions.pos.services.apiService

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

/**
 * Automatically refreshes tokens every 5 minutes while the app is in the foreground
 * and the user is logged in.
 */
class TokenAutoRefresher private constructor(
    private val context: Context
) : DefaultLifecycleObserver {

    private var refreshJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isUserLoggedIn = false

    /**
     * Call this after user successfully signs in to start the auto refresh
     */
    fun onUserSignedIn() {
        isUserLoggedIn = true
        startRefreshJob()
    }

    /**
     * Call this after user signs out to stop the auto refresh
     */
    fun onUserSignedOut() {
        isUserLoggedIn = false
        stopRefreshJob()
    }

    override fun onStart(owner: LifecycleOwner) {
        // App came to foreground
        println("TokenAutoRefresher: App in foreground")
        isUserLoggedIn = TokenRepository.getInstance(context.applicationContext).getAuthToken() != null
        println("TokenAutoRefresher: isUserLoggedIn: $isUserLoggedIn")
        if (isUserLoggedIn) {
            startRefreshJob()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        // App went to background
        println("TokenAutoRefresher: App in background")
        stopRefreshJob()
    }

    private fun startRefreshJob() {
        if (refreshJob?.isActive == true) return // Already running

        refreshJob = scope.launch {
            while (isActive) {
                delay(6.minutes)
                if (isUserLoggedIn) {
                    try {
                        println("TokenAutoRefresher: Refreshing token...")
                        val tokenRepository = TokenRepository.getInstance(context)
                        tokenRepository.refreshTokenIfNeeded()
                        println("TokenAutoRefresher: Token refresh completed")
                    } catch (e: Exception) {
                        println("TokenAutoRefresher: Token refresh failed - ${e.message}")
                    }
                }
            }
        }
        println("TokenAutoRefresher: Refresh job started")
    }

    private fun stopRefreshJob() {
        refreshJob?.cancel()
        refreshJob = null
        println("TokenAutoRefresher: Refresh job stopped")
    }

    companion object {
        @Volatile
        private var INSTANCE: TokenAutoRefresher? = null

        fun getInstance(context: Context): TokenAutoRefresher =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenAutoRefresher(context.applicationContext).also {
                    INSTANCE = it
                    // Register with ProcessLifecycleOwner to observe app lifecycle
                    ProcessLifecycleOwner.get().lifecycle.addObserver(it)
                }
            }
    }
}


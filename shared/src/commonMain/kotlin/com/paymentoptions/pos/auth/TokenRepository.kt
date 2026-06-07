package com.paymentoptions.pos.auth

import com.paymentoptions.pos.network.TokenRefreshService
import com.paymentoptions.pos.platformLog
import com.paymentoptions.pos.platformLogError
import com.paymentoptions.pos.storage.AppStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock

private const val TAG = "TokenRepository"
/** Refresh 5 minutes before the token expires. */
private const val REFRESH_BUFFER_MS = 5 * 60 * 1_000L
/** Minimum gap between two refresh attempts to prevent storms on 429. */
private const val MIN_REFRESH_COOLDOWN_MS = 60 * 1_000L

/**
 * Manages token lifecycle: auto-refresh before expiry and 401 handling.
 *
 * The Ktor [Auth] plugin's `refreshTokens` block handles 401s automatically.
 * This object schedules a proactive refresh before the token actually expires.
 *
 * Only ONE refresh can be scheduled or in-flight at a time. Duplicate calls to
 * [scheduleProactiveRefresh] are safely collapsed into the existing job.
 */
object TokenRepository {

    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val refreshMutex = Mutex()
    private var refreshJob: Job? = null
    /** Timestamp (epoch ms) of the last completed refresh attempt. Protected by [refreshMutex]. */
    private var lastRefreshAttemptMs: Long = 0L

    fun scheduleProactiveRefresh(expiryEpochMs: Long) {
        val nowMs = Clock.System.now().toEpochMilliseconds()
        platformLog(TAG, "scheduleProactiveRefresh called: expiryEpochMs=$expiryEpochMs now=$nowMs")

        // If a job is already waiting or running, don't pile on another one.
        if (refreshJob?.isActive == true) {
            platformLog(TAG, "Refresh job already active, skipping duplicate schedule")
            return
        }

        val refreshAt = expiryEpochMs - REFRESH_BUFFER_MS
        val delayMs = (refreshAt - nowMs).coerceAtLeast(0L)

        platformLog(TAG, "Scheduling token refresh in ${delayMs / 1_000}s")
        refreshJob = scope.launch {
            if (delayMs > 0) delay(delayMs)
            refresh()
        }
    }

    /** Call after user signs out. A new scope is created so future logins work. */
    fun stop() {
        refreshJob?.cancel()
        refreshJob = null
        scope.cancel()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        lastRefreshAttemptMs = 0L
        platformLog(TAG, "TokenRepository stopped; scope recreated for next session")
    }

    private suspend fun refresh() {
        // Prevent parallel refresh calls — only one winner proceeds, others bail.
        if (!refreshMutex.tryLock()) {
            platformLog(TAG, "Refresh already in progress, skipping")
            return
        }
        try {
            val nowMs = Clock.System.now().toEpochMilliseconds()
            val sinceLastRefresh = nowMs - lastRefreshAttemptMs
            if (sinceLastRefresh < MIN_REFRESH_COOLDOWN_MS) {
                platformLog(TAG, "Refresh cooldown active (${sinceLastRefresh}ms since last attempt), skipping")
                return
            }
            lastRefreshAttemptMs = nowMs

            val result = TokenRefreshService.refreshToken()
            if (result != null) {
                platformLog(TAG, "Token refreshed proactively; scheduling next cycle")
                // Schedule the NEXT cycle for the freshly-stored expiry.
                // Clear the job ref first so the guard above doesn't block the new schedule.
                refreshJob = null
                scheduleProactiveRefresh(AppStorage.tokenExpiry)
            } else {
                platformLog(TAG, "Proactive refresh returned null – user may need to re-authenticate")
                AuthEventManager.requireReAuthentication()
            }
        } catch (e: Exception) {
            platformLogError(TAG, "Proactive refresh failed", e)
            AuthEventManager.requireManualSignIn()
        } finally {
            refreshMutex.unlock()
        }
    }
}

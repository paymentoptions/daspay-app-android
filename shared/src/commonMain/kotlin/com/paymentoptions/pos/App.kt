package com.paymentoptions.pos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.paymentoptions.pos.auth.TokenRepository
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.ui.navigation.Navigator
import com.paymentoptions.pos.ui.theme.AppTheme
import kotlinx.datetime.Clock

/**
 * Root Compose Multiplatform composable.
 *
 * Called from [MainActivity] on Android and [MainViewController] on iOS.
 *
 * @param buildTimeBaseUrl  The compile-time base URL from BuildConfig / plist.
 *                          Defaults to production if not supplied.
 */
@Composable
fun App(buildTimeBaseUrl: String = "https://api-dev.paymentoptions.com/api/v1/") {

    // Inject build-time base URL so ConfigurationManager can resolve API calls
    LaunchedEffect(Unit) {
        ConfigurationManager.buildTimeBaseUrl = buildTimeBaseUrl

        // Schedule proactive token refresh only if the token still has meaningful lifetime left.
        // Expired / near-expired tokens are handled reactively by the Ktor 401 refreshTokens path.
        val expiry = AppStorage.tokenExpiry
        val nowMs = Clock.System.now().toEpochMilliseconds()
        val REFRESH_BUFFER_MS = 5 * 60 * 1_000L
        if (expiry > 0L && expiry - nowMs > REFRESH_BUFFER_MS) {
            TokenRepository.scheduleProactiveRefresh(expiry)
        }

        // Lock the screen to portrait on supported platforms
        lockPortrait()
    }

    AppTheme {
        Navigator()
    }
}

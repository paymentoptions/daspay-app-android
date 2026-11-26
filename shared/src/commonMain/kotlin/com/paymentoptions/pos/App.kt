package com.paymentoptions.pos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.paymentoptions.pos.auth.TokenRepository
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.payment.reader.ReaderLifecycleCoordinator
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.ui.navigation.Navigator
import com.paymentoptions.pos.ui.theme.AppTheme
import kotlinx.datetime.Clock
import androidx.compose.runtime.remember

/**
 * Root Compose Multiplatform composable.
 *
 * Called from [MainActivity] on Android and [MainViewController] on iOS.
 *
 * @param buildTimeBaseUrl  The compile-time base URL from BuildConfig / plist.
 * @param environment       Backend environment label (DEV, STAGING, PROD).
 */
@Composable
fun App(
    buildTimeBaseUrl: String = "https://api-dev.paymentoptions.com/api/v1/",
    environment: String = "DEV",
) {

    // Inject the build-time base URL and environment during composition, before Navigator's first
    // screen reads them (a LaunchedEffect runs too late: Splash captured the default "DEV").
    remember(buildTimeBaseUrl, environment) {
        ConfigurationManager.buildTimeBaseUrl =
            if (ConfigurationManager.isUsableBaseUrl(buildTimeBaseUrl)) buildTimeBaseUrl.trim()
            else ConfigurationManager.DEFAULT_BASE_URL
        ConfigurationManager.buildEnvironment = environment
    }

    LaunchedEffect(buildTimeBaseUrl, environment) {

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

        // Resume the Tap to Pay reader if a session was already active (app relaunch).
        ReaderLifecycleCoordinator.appStarted()
    }

    // App-wide foreground hook — the single place the reader is warmed up on resume.
    OnResume {
        ReaderLifecycleCoordinator.onForeground()
    }

    AppTheme {
        Navigator()
    }
}

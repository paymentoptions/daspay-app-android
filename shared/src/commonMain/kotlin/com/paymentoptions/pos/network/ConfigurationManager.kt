package com.paymentoptions.pos.network

import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.endpoints.getAppConfiguration
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.urlEncode
import kotlinx.coroutines.delay
import kotlin.concurrent.Volatile
import kotlin.coroutines.cancellation.CancellationException

/**
 * Manages the active API base URL.
 *
 * Priority: saved config → AppStorage override → the flavour's build-time (bootstrap) URL.
 */
object ConfigurationManager {

    private const val MAX_RETRIES = 3
    private const val RETRY_DELAY_MS = 2_000L

    /**
     * The flavour's bootstrap URL, injected at app startup from BuildConfig (Android) / plist (iOS).
     * Config is always downloaded from here, and it is the API base URL until config has been saved.
     * Never mutated after startup.
     */
    var buildTimeBaseUrl: String = "https://api-dev.paymentoptions.com/api/v1/"

    val bootstrapBaseUrl: String get() = buildTimeBaseUrl

    /** Injected at app startup from BuildConfig (Android) / plist (iOS). */
    var buildEnvironment: String = "DEV"

    @Volatile
    var isInitialized: Boolean = false
        private set

    /**
     * Downloads and saves the app config. Needs a signed-in user (the config endpoint is
     * authenticated), so it returns false straight away when there is no ID token.
     */
    suspend fun initializeConfig(environment: String = buildEnvironment): Boolean {
        if (AppStorage.idToken.isNullOrBlank()) {
            AppLogger.debug("Config download skipped: no signed-in user yet")
            return false
        }
        repeat(MAX_RETRIES) { attempt ->
            try {
                val appConfig = getAppConfiguration(environment)
                    ?.takeIf { it.success }
                    ?.data
                    ?.firstOrNull()
                    // A config without these URLs is treated as a failed download, so it's retried
                    // instead of saved.
                    ?.takeIf { !it.BaseAPIURL.isNullOrBlank() && !it.TransactionDetailsURL.isNullOrBlank() }

                if (appConfig != null) {
                    AppLogger.debug("Config downloaded. Base URL: ${appConfig.BaseAPIURL}")

                    val newBaseUrl = appConfig.BaseAPIURL
                    val baseUrlChanged = !newBaseUrl.isNullOrBlank() &&
                            DPStorageManager.getBaseUrl()?.trimEnd('/') != newBaseUrl.trimEnd('/')

                    // Always persist (storeAppConfig skips blank fields), so
                    // TransactionDetailsURL / PayByLinkURL get saved even when the base URL is unchanged.
                    DPStorageManager.storeAppConfig(appConfig)

                    if (baseUrlChanged) {
                        // KtorClient resolves the base URL per request via url(), so the client does
                        // not need rebuilding (unlike the old RetrofitClient).
                        AppLogger.debug("Config changed base url to $activeBaseUrl")
                    }

                    isInitialized = true
                    return true
                }
                AppLogger.error("Failed to download app configuration, or it was missing required URLs (Attempt ${attempt + 1})")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLogger.error("Error initializing config (Attempt ${attempt + 1}): ${e.message}")
            }
            if (attempt < MAX_RETRIES - 1) delay(RETRY_DELAY_MS)
        }
        return false
    }

    val activeBaseUrl: String
        get() = DPStorageManager.getBaseUrl()
            ?: AppStorage.configBaseUrl?.takeIf { it.isNotBlank() }
            ?: buildTimeBaseUrl

    fun url(path: String): String {
        val base = activeBaseUrl.trimEnd('/')
        val segment = path.trimStart('/')
        return "$base/$segment"
    }

    /** Resolves a templated path like `entities/{merchantId}` with path params. */
    fun url(path: String, vararg pathParams: Pair<String, String>): String {
        val resolvedPath = pathParams.fold(path) { acc, (key, value) ->
            acc.replace("{$key}", urlEncode(value))
        }
        return url(resolvedPath)
    }
}
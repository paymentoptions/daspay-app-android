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
 * Priority: saved config → AppStorage override → build-time default.
 */
object ConfigurationManager {

    private const val MAX_RETRIES = 3
    private const val RETRY_DELAY_MS = 2_000L

    /** Injected at app startup from BuildConfig (Android) / plist (iOS). Never mutated after that. */
    var buildTimeBaseUrl: String = "https://api-dev.paymentoptions.com/api/v1/"

    /** Injected at app startup from BuildConfig (Android) / plist (iOS). */
    var buildEnvironment: String = "DEV"

    @Volatile
    var isInitialized: Boolean = false
        private set

    suspend fun initializeConfig(environment: String = buildEnvironment): Boolean {
        repeat(MAX_RETRIES) { attempt ->
            try {
                val appConfig = getAppConfiguration(environment)
                    ?.takeIf { it.success }
                    ?.data
                    ?.firstOrNull()

                if (appConfig != null) {
                    AppLogger.debug("Config downloaded. Base URL: ${appConfig.BaseAPIURL}")

                    val newBaseUrl = appConfig.BaseAPIURL
                    val baseUrlChanged = !newBaseUrl.isNullOrBlank() &&
                            DPStorageManager.getBaseUrl()?.trimEnd('/') != newBaseUrl.trimEnd('/')

                    // Always persist (storeAppConfig skips blank fields), so
                    // TransactionDetailsURL / PayByLinkURL get saved even when the base URL is unchanged.
                    DPStorageManager.storeAppConfig(appConfig)

                    if (baseUrlChanged) {
                        // Notify KtorClient that the base URL may have changed
                        buildTimeBaseUrl = DPStorageManager.getBaseUrl() ?: appConfig.BaseAPIURL

                        AppLogger.debug("Config changed base url, resetting HTTP client")
                        KtorClient.reset() // only needed if the client caches a base URL (see note)
                    }

                    isInitialized = true
                    return true
                }
                AppLogger.error("Failed to download app configuration (Attempt ${attempt + 1})")
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
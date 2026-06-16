package com.paymentoptions.pos.network

import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.endpoints.getAppConfiguration
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.urlEncode
import org.jetbrains.compose.resources.ResourceEnvironment

/**
 * Manages the active API base URL.
 *
 * Priority: AppStorage override → build-time default.
 */
object ConfigurationManager {

    /** Injected at app startup from BuildConfig (Android) / plist (iOS). */
    var buildTimeBaseUrl: String = "https://api-dev.paymentoptions.com/api/v1/"

    suspend fun initializeConfig(environment: String): Boolean {
        return try {
            val appConfig = getAppConfiguration(environment)
                ?.takeIf { it.success }
                ?.data
                ?.firstOrNull() ?: run {
                AppLogger.error("Failed to download app configuration")
                return false
            }
            AppLogger.debug("Config downloaded. Base URL: ${appConfig.BaseAPIURL}")
            val savedBaseUrl = DPStorageManager.getBaseUrl()
            if (savedBaseUrl != appConfig.BaseAPIURL) {
                DPStorageManager.storeAppConfig(appConfig)
                // Notify KtorClient that the base URL may have changed
                buildTimeBaseUrl = DPStorageManager.getBaseUrl() ?: appConfig.BaseAPIURL
            }
            true
        } catch (e: Exception) {
            AppLogger.error("Error initializing config: ${e.message}")
            false
        }
    }

    val activeBaseUrl: String
        get() = AppStorage.baseUrl?.takeIf { it.isNotBlank() }
            ?: AppStorage.configBaseUrl?.takeIf { it.isNotBlank() }
            ?: buildTimeBaseUrl

    fun url(path: String): String {
        val base = "https://api-dev.paymentoptions.com/api/v1/".trimEnd('/')
        val segment = path.trimStart('/')
        return "$base/$segment"
    }

    /**
     * Resolves a templated path like `entities/{merchantId}` with path params.
     */
    fun url(path: String, vararg pathParams: Pair<String, String>): String {
        val resolvedPath = pathParams.fold(path) { acc, (key, value) ->
            acc.replace("{$key}", urlEncode(value))
        }
        return url(resolvedPath)
    }
}

package com.paymentoptions.pos.services.apiService

import android.content.Context
import com.paymentoptions.pos.BuildConfig
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.endpoints.getAppConfiguration

/**
 * Delegates to [com.paymentoptions.pos.network.ConfigurationManager] (shared module).
 * Downloads app configuration and stores it via [DPStorageManager] → [com.paymentoptions.pos.storage.AppStorage].
 */
object ConfigurationManager {

    suspend fun initializeConfig(context: Context): Boolean {
        return try {
            val appConfig = getAppConfiguration(BuildConfig.ENVIRONMENT)
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
                com.paymentoptions.pos.network.ConfigurationManager.buildTimeBaseUrl =
                    com.paymentoptions.pos.network.ConfigurationManager.buildTimeBaseUrl
            }
            true
        } catch (e: Exception) {
            AppLogger.error("Error initializing config: ${e.message}")
            false
        }
    }

    suspend fun refreshConfig(context: Context): Boolean {
        return try {
            val appConfig = getAppConfiguration(BuildConfig.ENVIRONMENT)
                ?.takeIf { it.success }
                ?.data
                ?.firstOrNull() ?: run {
                AppLogger.error("Failed to refresh app configuration")
                return false
            }
            AppLogger.debug("Config refreshed. Base URL: ${appConfig.BaseAPIURL}")
            DPStorageManager.storeAppConfig(appConfig)
            true
        } catch (e: Exception) {
            AppLogger.error("Error refreshing config: ${e.message}")
            false
        }
    }

    fun getCurrentEnvironment(): String = BuildConfig.ENVIRONMENT

    fun getEffectiveBaseUrl(context: Context): String =
        DPStorageManager.getBaseUrl()?.takeIf { it.isNotEmpty() }
            ?: BuildConfig.CONFIG_BASE_URL
}

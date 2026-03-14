package com.paymentoptions.pos.services.apiService

import android.content.Context
import com.paymentoptions.pos.BuildConfig
import com.paymentoptions.pos.device.DPSharedPreferences
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.endpoints.configDownload

object ConfigurationManager {

    @Volatile
    private var isInitialized = false

    /**
     * Initialize app configuration by checking if base URL is saved.
     * If not saved, download config from API based on current environment.
     */
    suspend fun initializeConfig(context: Context): Boolean {
        return try {
            // Download config for current flavor/environment
            val appConfig = configDownload(context, BuildConfig.ENVIRONMENT)

            if (appConfig != null) {
                AppLogger.debug("Config downloaded successfully. Base URL: ${appConfig.BaseAPIURL}")

                val savedBaseUrl = DPSharedPreferences.getBaseUrl(context)
                if(savedBaseUrl != appConfig.BaseAPIURL)
                    DPSharedPreferences.storeBaseUrl(context, appConfig.BaseAPIURL)

                // Reset RetrofitClient to use new base URL
                RetrofitClient.reset()

                isInitialized = true
                true
            } else {
                AppLogger.error("Failed to download app configuration")
                false
            }
//            val savedBaseUrl = DPSharedPreferences.getBaseUrl(context)
//
//            if (savedBaseUrl.isNullOrEmpty()) {
//                AppLogger.debug("Base URL not found in preferences. Downloading config for environment: ${BuildConfig.ENVIRONMENT}")
//
//
//            } else {
//                AppLogger.debug("Using saved base URL from preferences: $savedBaseUrl")
//                isInitialized = true
//                true
//            }
        } catch (e: Exception) {
            AppLogger.error("Error initializing config: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Force refresh of configuration from API
     */
    suspend fun refreshConfig(context: Context): Boolean {
        return try {
            AppLogger.debug("Forcing config refresh for environment: ${BuildConfig.ENVIRONMENT}")

            val appConfig = configDownload(context, BuildConfig.ENVIRONMENT)

            if (appConfig != null) {
                AppLogger.debug("Config refreshed successfully. Base URL: ${appConfig.BaseAPIURL}")
                DPSharedPreferences.storeBaseUrl(context, appConfig.BaseAPIURL)

                // Reset RetrofitClient to use new base URL
                RetrofitClient.reset()

                true
            } else {
                AppLogger.error("Failed to refresh app configuration")
                false
            }
        } catch (e: Exception) {
            AppLogger.error("Error refreshing config: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Get current environment name
     */
    fun getCurrentEnvironment(): String {
        return BuildConfig.ENVIRONMENT
    }

    /**
     * Check if configuration is initialized
     */
    fun isConfigInitialized(): Boolean {
        return isInitialized
    }

    /**
     * Get the base URL being used (from preferences or BuildConfig)
     */
    fun getEffectiveBaseUrl(context: Context): String {
        val savedBaseUrl = DPSharedPreferences.getBaseUrl(context)
        return if (!savedBaseUrl.isNullOrEmpty()) {
            savedBaseUrl
        } else {
            BuildConfig.CONFIG_BASE_URL
        }
    }
}


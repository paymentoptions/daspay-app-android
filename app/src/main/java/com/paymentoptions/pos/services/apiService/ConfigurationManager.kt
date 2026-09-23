package com.paymentoptions.pos.services.apiService

import android.content.Context
import com.paymentoptions.pos.BuildConfig
import com.paymentoptions.pos.device.DPSharedPreferences
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.endpoints.configDownload


/**
 * {"statusCode":200,"message":"successful","messageCode":"INFO_CONFIG_0000",
 * "success":true,"data":[{"ID":5,"AppENV":"DEV","BaseAPIURL":"https://api-dev.paymentoptions.com/api/v1",
 * "RegistryLogin":"DEV_LOGIN","RegistryToken":"DEV_TOKEN","PrevAppVersion":1,"CurrAppVersion":1,
 * "IsUpdateMandatory":true,"TransactionDetailsURL":"https://me.paymentoptions.com/daspay-transaction-details/"}]}
 *
 */
object ConfigurationManager {

    @Volatile
    private var isInitialized = false

    /**
     * Initialize app configuration by checking if base URL is saved.
     * If not saved, download config from API based on current environment.
     */
    suspend fun initializeConfig(context: Context): Boolean {
        var retryCount = 0
        val maxRetries = 3
        
        while (retryCount < maxRetries) {
            try {
                // Download config for current flavor/environment
                val appConfig = configDownload(context, BuildConfig.ENVIRONMENT)

                if (appConfig != null) {
                    AppLogger.debug("Config downloaded successfully. Base URL: ${appConfig.BaseAPIURL}")

                    val savedBaseUrl = DPSharedPreferences.getBaseUrl(context)
                    val baseUrlChanged = savedBaseUrl != "${appConfig.BaseAPIURL}/"

                    // Always persist the freshly downloaded config (storeAppConfig skips blank
                    // fields), not just when BaseAPIURL changes - otherwise fields like
                    // TransactionDetailsURL/PayByLinkURL never get saved once the base URL settles.
                    DPSharedPreferences.storeAppConfig(context, appConfig)

                    if (baseUrlChanged) {
                        // Reset RetrofitClient to use new base URL
                        AppLogger.debug("Config changed base url, triggering reset of retrofit")
                        RetrofitClient.reset()
                    }

                    isInitialized = true
                    return true
                } else {
                    AppLogger.error("Failed to download app configuration (Attempt ${retryCount + 1})")
                }
            } catch (e: Exception) {
                AppLogger.error("Error initializing config (Attempt ${retryCount + 1}): ${e.message}")
                if (retryCount == maxRetries - 1) {
                    e.printStackTrace()
                }
            }
            retryCount++
            if (retryCount < maxRetries) {
                kotlinx.coroutines.delay(2000) // Wait 2 seconds before retry
            }
        }
        return false
    }

//    /**
//     * Force refresh of configuration from API
//     */
//    suspend fun refreshConfig(context: Context): Boolean {
//        var retryCount = 0
//        val maxRetries = 3
//
//        while (retryCount < maxRetries) {
//            try {
//                AppLogger.debug("Forcing config refresh for environment: ${BuildConfig.ENVIRONMENT}")
//
//                val appConfig = configDownload(context, BuildConfig.ENVIRONMENT)
//
//                if (appConfig != null) {
//                    AppLogger.debug("Config refreshed successfully. Base URL: ${appConfig.BaseAPIURL}")
//                    DPSharedPreferences.storeAppConfig(context, appConfig)
//
//                    // Reset RetrofitClient to use new base URL
//                    RetrofitClient.reset()
//
//                    return true
//                } else {
//                    AppLogger.error("Failed to refresh app configuration (Attempt ${retryCount + 1})")
//                }
//            } catch (e: Exception) {
//                AppLogger.error("Error refreshing config (Attempt ${retryCount + 1}): ${e.message}")
//            }
//            retryCount++
//            if (retryCount < maxRetries) {
//                kotlinx.coroutines.delay(2000)
//            }
//        }
//        return false
//    }
//
//    /**
//     * Get current environment name
//     */
//    fun getCurrentEnvironment(): String {
//        return BuildConfig.ENVIRONMENT
//    }
//
//    /**
//     * Check if configuration is initialized
//     */
//    fun isConfigInitialized(): Boolean {
//        return isInitialized
//    }

//    /**
//     * Get the base URL being used (from preferences or BuildConfig)
//     */
//    fun getEffectiveBaseUrl(context: Context): String {
//        val savedBaseUrl = DPSharedPreferences.getBaseUrl(context)
//        return if (!savedBaseUrl.isNullOrEmpty()) {
//            savedBaseUrl
//        } else {
//            BuildConfig.CONFIG_BASE_URL
//        }
//    }
}


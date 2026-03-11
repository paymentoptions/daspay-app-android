package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.DPSharedPreferences
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.AppConfig
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generateRequestHeader

suspend fun configDownload(
    context: Context,
    flavourName: String
): AppConfig? {
    try {
        val appConfigResponse = RetrofitClient.getApi(context).getAppConfiguration(flavourName = flavourName)

        if (appConfigResponse.success && appConfigResponse.data.isNotEmpty()) {
            // Store app config base url in shared preference
            val appConfig = appConfigResponse.data.first()
            AppLogger.debug("configDownload: Config downloaded successfully for $flavourName - Base URL: ${appConfig.BaseAPIURL}")
            return appConfig
        } else {
            AppLogger.error("configDownload: No config data found for $flavourName")
            return null
        }
    } catch (e: Exception) {
        AppLogger.error("configDownload: Error - ${e.message}")
        e.printStackTrace()
        throw e
    }
}


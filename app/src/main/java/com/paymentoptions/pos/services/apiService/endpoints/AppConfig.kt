package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
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

        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded() ?: return null

        val idToken = authDetails?.data?.token?.idToken
        val requestHeaders = generateRequestHeader(idToken ?: "")
        val appConfigResponse = RetrofitClient.getConfigApi(context)
            .getAppConfiguration(headers = requestHeaders,flavourName = flavourName)

        if (appConfigResponse.success && appConfigResponse.data.isNotEmpty()) {
            val appConfig = appConfigResponse.data.first()
            if (appConfig.BaseAPIURL.isNullOrBlank() || appConfig.TransactionDetailsURL.isNullOrBlank()) {
                // Response parsed but key fields are missing/blank (e.g. partial JSON body) -
                // treat as a failed download so the caller retries instead of caching this.
                AppLogger.error("configDownload: Config response for $flavourName is missing required fields")
                return null
            }
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


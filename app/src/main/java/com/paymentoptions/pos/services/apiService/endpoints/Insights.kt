package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.InsightsResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generateRequestHeader
import com.paymentoptions.pos.utils.getDeviceIdentifier
import com.paymentoptions.pos.utils.getDeviceTimeZone

suspend fun insights(
    context: Context,
    startDate: String,
    endDate: String,
    take: Int = -1,
): InsightsResponse? {
    try {
        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded() ?: return null

        val idToken = authDetails.data.token.idToken
        val requestHeaders = generateRequestHeader(idToken)

        val deviceNumber = getDeviceIdentifier(context)
        val timeZone = getDeviceTimeZone()
        val tokenCode = SharedPreferences.getTokenStatus(context = context).second

        AppLogger.debug("insights request: deviceNumber = $deviceNumber | uniqueCode = $tokenCode | timeZone = $timeZone | startDate = $startDate | endDate = $endDate | take = $take")

        val response = RetrofitClient.getApi(context).insights(
            headers = requestHeaders,
            deviceNumber = deviceNumber,
            uniqueCode = tokenCode,
//            timeZone = timeZone,
            startDate = startDate,
            endDate = endDate,
            take = take,
        )

        AppLogger.debug("insights response : $response")

        return response
    } catch (e: Exception) {
        AppLogger.debug("insightsError: $e")
        throw e
    }
}
package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.InsightsResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.generateRequestHeader
import com.paymentoptions.pos.services.apiService.shouldRefreshToken
import com.paymentoptions.pos.utils.getDeviceIdentifier
import com.paymentoptions.pos.utils.getDeviceTimeZone

suspend fun insights(
    context: Context,
    startDate: String,
    endDate: String,
    take: Int = -1,
): InsightsResponse? {
    try {
        var authDetails = SharedPreferences.getAuthDetails(context)
        val username = authDetails?.data?.email ?: ""
        val refreshToken = authDetails?.data?.token?.refreshToken ?: ""
        val shouldRefreshToken = shouldRefreshToken(authDetails?.data?.exp)

        if (shouldRefreshToken) authDetails = refreshTokens(context, username, refreshToken)

        val idToken = authDetails?.data?.token?.idToken
        val requestHeaders = generateRequestHeader(idToken ?: "")

        val deviceNumber = getDeviceIdentifier(context)
        val timeZone = getDeviceTimeZone()
        val tokenCode = SharedPreferences.getTokenStatus(context = context).second

        println("insights request: deviceNumber = $deviceNumber | uniqueCode = $tokenCode | timeZone = $timeZone | startDate = $startDate | endDate = $endDate | take = $take")

        val response = RetrofitClient.api.insights(
            headers = requestHeaders,
            deviceNumber = deviceNumber,
            uniqueCode = tokenCode,
            timeZone = timeZone,
            startDate = startDate,
            endDate = endDate,
            take = take,
        )

        println("insights response : $response")

        return response
    } catch (e: Exception) {
        println("insightsError: $e")
        throw e
    }
}
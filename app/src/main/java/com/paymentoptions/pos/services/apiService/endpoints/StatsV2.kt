package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.StatsV2Request
import com.paymentoptions.pos.services.apiService.StatsV2Response
import com.paymentoptions.pos.services.apiService.generateRefundRequestHeaders
import com.paymentoptions.pos.services.apiService.shouldRefreshToken

suspend fun statsV2(
    context: Context,
    request: StatsV2Request,
): StatsV2Response? {

    try {
        var authDetails = SharedPreferences.getAuthDetails(context)
        val username = authDetails?.data?.email ?: ""
        val refreshToken = authDetails?.data?.token?.refreshToken ?: ""
        val shouldRefreshToken = shouldRefreshToken(authDetails?.data?.exp)

        if (shouldRefreshToken) authDetails = refreshTokens(context, username, refreshToken)

        val idToken = authDetails?.data?.token?.idToken
        val requestHeaders = generateRefundRequestHeaders(idToken ?: "")

        val response: StatsV2Response =
            RetrofitClient.api.statsV2(headers = requestHeaders, request = request)

        println("statsV2Response: $response")
        return response
    } catch (e: Exception) {
        println("RefundError: $e")
        throw e
    }
}
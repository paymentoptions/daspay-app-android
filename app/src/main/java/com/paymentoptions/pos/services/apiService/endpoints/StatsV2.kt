package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.StatsV2Request
import com.paymentoptions.pos.services.apiService.StatsV2Response
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generateRefundRequestHeader

suspend fun statsV2(
    context: Context,
    request: StatsV2Request,
): StatsV2Response? {

    try {
        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded()

        val idToken = authDetails?.data?.token?.idToken
        val requestHeaders = generateRefundRequestHeader(idToken ?: "")

        val response: StatsV2Response =
            RetrofitClient.getApi(context).statsV2(headers = requestHeaders, request = request)

        AppLogger.debug("statsV2Response: $response")
        return response
    } catch (e: Exception) {
        AppLogger.error("RefundError: $e")
        throw e
    }
}
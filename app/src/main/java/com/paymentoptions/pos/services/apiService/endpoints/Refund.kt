package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.RefundRequest
import com.paymentoptions.pos.services.apiService.RefundResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.generateRequestHeaders
import com.paymentoptions.pos.services.apiService.shouldRefreshToken

suspend fun refund(
    context: Context,
    refundRequest: RefundRequest,
): RefundResponse? {

    try {
        var authDetails = SharedPreferences.getAuthDetails(context)
        val shouldRefreshToken = shouldRefreshToken(authDetails?.data?.exp)

        if (shouldRefreshToken) {
            val username = authDetails?.data?.email ?: ""
            val refreshToken = authDetails?.data?.token?.refreshToken ?: ""
            authDetails = refreshTokens(context, username, refreshToken)
        }

        val idToken = authDetails?.data?.token?.idToken
        val requestHeaders = generateRequestHeaders(idToken ?: "")

        println("refundRequest: $refundRequest | $authDetails")
        var refundResponse: RefundResponse? =
            RetrofitClient.api.refund(requestHeaders, refundRequest)

        println("refundResponse: $refundResponse")
        return refundResponse
    } catch (e: Exception) {
        println("RefundError: $e")
        throw e
    }
}
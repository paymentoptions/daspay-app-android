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
    transactionId: String,
    merchantId: String,
    amount: Float,
): RefundResponse? {
    try {
        var authDetails = SharedPreferences.getAuthDetails(context)
        val username = authDetails?.data?.email ?: ""
        val refreshToken = authDetails?.data?.token?.refreshToken ?: ""
        val shouldRefreshToken = shouldRefreshToken(authDetails)

        if (shouldRefreshToken) {
            val refreshTokenResponse = refreshTokens(username, refreshToken)

            if (refreshTokenResponse == null) {
                SharedPreferences.clearSharedPreferences(context)
                return null
            } else
                SharedPreferences.saveAuthDetails(context, refreshTokenResponse)
        }

        authDetails = SharedPreferences.getAuthDetails(context)
        val idToken = authDetails?.data?.token?.idToken

        val requestHeaders =
            generateRequestHeaders(idToken ?: "")
        val refundRequest = RefundRequest(transactionId, merchantId, amount)

        var refundResponse: RefundResponse? = null
        refundResponse = RetrofitClient.api.refund(requestHeaders, refundRequest)
        return refundResponse
    } catch (e: Exception) {
        println("RefundError: $e")
        throw e
    }
}
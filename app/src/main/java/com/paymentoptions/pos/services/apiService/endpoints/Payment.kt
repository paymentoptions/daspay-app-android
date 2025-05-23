package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.PaymentRequest
import com.paymentoptions.pos.services.apiService.PaymentResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.generateRequestHeaders
import com.paymentoptions.pos.services.apiService.shouldRefreshToken

suspend fun payment(
    context: Context,
    paymentRequest: PaymentRequest,
): PaymentResponse? {
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

        var paymentResponse: PaymentResponse? = null
        paymentResponse = RetrofitClient.api.payment(requestHeaders, paymentRequest)

        return paymentResponse
    } catch (e: Exception) {
        println("paymentError: $e")
        throw e
    }
}
package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.PaymentRequest
import com.paymentoptions.pos.services.apiService.PaymentResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.generatePaymentRequestHeaders
import com.paymentoptions.pos.services.apiService.shouldRefreshToken

suspend fun payment(
    context: Context,
    paymentRequest: PaymentRequest,
): PaymentResponse? {
    try {
        var authDetails = SharedPreferences.getAuthDetails(context)
        val username = authDetails?.data?.email ?: ""
        val refreshToken = authDetails?.data?.token?.refreshToken ?: ""
        val shouldRefreshToken = shouldRefreshToken(authDetails?.data?.exp)

        if (shouldRefreshToken) authDetails = refreshTokens(context, username, refreshToken)

        val idToken = authDetails?.data?.token?.idToken
        val requestHeaders = generatePaymentRequestHeaders(idToken ?: "")

        var paymentResponse: PaymentResponse =
            RetrofitClient.api.payment(headers = requestHeaders, request = paymentRequest)

        return paymentResponse
    } catch (e: Exception) {
        println("paymentError: $e")
        throw e
    }
}
package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.PaymentDetailsResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.generateRequestHeader
import com.paymentoptions.pos.services.apiService.shouldRefreshToken

suspend fun paymentDetails(
    context: Context,
    paymentId: String,
): PaymentDetailsResponse? {
    try {
        var authDetails = SharedPreferences.getAuthDetails(context)
        val username = authDetails?.data?.email ?: ""
        val refreshToken = authDetails?.data?.token?.refreshToken ?: ""
        val shouldRefreshToken = shouldRefreshToken(authDetails?.data?.exp)

        if (shouldRefreshToken) authDetails = refreshTokens(context, username, refreshToken)

        val idToken = authDetails?.data?.token?.idToken ?: ""
        val requestHeaders = generateRequestHeader(authToken = idToken)

        println("PaymentDetails payment Id -->: $paymentId")
        var response: PaymentDetailsResponse =
            RetrofitClient.api.paymentDetails(headers = requestHeaders, paymentId = paymentId)

        println("PaymentDetails response -->: $response")

        return response
    } catch (e: Exception) {
        println("PaymentDetails error: ${e.stackTrace}")
        throw e
    }
}
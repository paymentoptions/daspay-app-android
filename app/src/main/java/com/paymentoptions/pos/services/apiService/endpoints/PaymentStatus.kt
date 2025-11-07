package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.PaymentStatusRequest
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.generatePaymentStatusHeader
import com.paymentoptions.pos.services.apiService.shouldRefreshToken

suspend fun paymentStatus(
    context: Context,
    request: PaymentStatusRequest,
): Boolean {
    try {
        var authDetails = SharedPreferences.getAuthDetails(context)
        val username = authDetails?.data?.email ?: ""
        val refreshToken = authDetails?.data?.token?.refreshToken ?: ""
        val shouldRefreshToken = shouldRefreshToken(authDetails?.data?.exp)

        if (shouldRefreshToken) authDetails = refreshTokens(context, username, refreshToken)

        val requestHeaders = generatePaymentStatusHeader()

        println("inThis PaymentStatus request -->: $request")
        var response: String =
            RetrofitClient.api.paymentStatus(headers = requestHeaders, request = request)

        println("inThis PaymentStatus response -->: $response")

        return response.uppercase() == "SUCCESS"
    } catch (e: Exception) {
        println("paymentStatusError: ${e.stackTrace}")
        throw e
    }
}
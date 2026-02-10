package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.PaymentDetailsResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generateRequestHeader
import com.paymentoptions.pos.services.apiService.shouldRefreshToken

suspend fun paymentDetails(
    context: Context,
    paymentId: String,
): PaymentDetailsResponse? {
    try {
        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded() ?: return null

        val idToken = authDetails.data.token.idToken
        val requestHeaders = generateRequestHeader(authToken = idToken)

        val response: PaymentDetailsResponse =
            RetrofitClient.getApi(context).paymentDetails(headers = requestHeaders, paymentId = paymentId)

        println("PaymentDetails: $response")

        return response
    } catch (e: Exception) {
        println("PaymentDetails error: ${e.stackTrace}")
        throw e
    }
}
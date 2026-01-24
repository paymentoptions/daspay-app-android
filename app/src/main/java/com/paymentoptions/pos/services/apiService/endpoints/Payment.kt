package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.services.apiService.PaymentRequest
import com.paymentoptions.pos.services.apiService.PaymentResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generatePaymentRequestHeader

suspend fun payment(
    context: Context,
    paymentRequest: PaymentRequest,
): PaymentResponse? {
    try {
        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded() ?: return null

        val idToken = authDetails.data.token.idToken
        val requestHeaders = generatePaymentRequestHeader(idToken)

        var paymentResponse: PaymentResponse =
            RetrofitClient.getApi(context).payment(headers = requestHeaders, request = paymentRequest)

        return paymentResponse
    } catch (e: Exception) {
        println("paymentError: $e")
        throw e
    }
}
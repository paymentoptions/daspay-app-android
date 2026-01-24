package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.services.apiService.RefundRequest
import com.paymentoptions.pos.services.apiService.RefundResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generateRefundRequestHeader

suspend fun refund(
    context: Context,
    refundRequest: RefundRequest,
): RefundResponse? {

    try {
        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded() ?: return null

        val idToken = authDetails.data.token.idToken
        val requestHeaders = generateRefundRequestHeader(idToken)

        println("refund request: $refundRequest | $authDetails")
        val refundResponse: RefundResponse =
            RetrofitClient.getApi(context).refund(headers = requestHeaders, request = refundRequest)

        println("refund response: $refundResponse")
        return refundResponse
    } catch (e: Exception) {
        println("refund error: $e")
        throw e
    }
}
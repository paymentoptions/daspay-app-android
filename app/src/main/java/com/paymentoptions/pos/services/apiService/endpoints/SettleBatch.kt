package com.paymentoptions.pos.services.apiService.endpoints


import android.content.Context
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.PaymentResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.SettleBatchRequest
import com.paymentoptions.pos.services.apiService.SettleBatchResponse
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generatePaymentRequestHeader

suspend fun settleBatch(
    context: Context,
    batchId: String,
): SettleBatchResponse? {
    val tokenRepository = TokenRepository.getInstance(context)
    val authDetails = tokenRepository.refreshTokenIfNeeded() ?: return null

    val idToken = authDetails.data.token.idToken
    val requestHeaders = generatePaymentRequestHeader(idToken)

    val settleBatchResponse: SettleBatchResponse? =
            RetrofitClient.getApi(context).settleBatch(headers = requestHeaders,
                request = SettleBatchRequest(batchId = batchId))

    return settleBatchResponse
}
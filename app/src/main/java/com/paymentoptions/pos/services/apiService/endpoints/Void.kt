package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.RefundRequest
import com.paymentoptions.pos.services.apiService.RefundResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generateRefundRequestHeader
import com.paymentoptions.pos.services.apiService.generateRequestHeader
import com.theminesec.lib.dto.transaction.TransactionRequest
import com.theminesec.sdk.headless.model.transaction.Transaction

suspend fun void(
    context: Context,
    transactionId: String,
    merchantId: String,
    transaction: com.theminesec.lib.dto.transaction.Transaction,
): RefundResponse? {

        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded() ?: return null

        val idToken = authDetails.data.token.idToken
        val requestHeaders = generateRequestHeader(idToken)

        AppLogger.debug("void request: $transaction | $authDetails")
        val refundResponse: RefundResponse =
            RetrofitClient.getApi(context).void(
                headers = requestHeaders,
                request = com.paymentoptions.pos.services.apiService.TransactionRequest(
                    transactionId = transactionId,
                    merchant_id = merchantId,
                    daspay_res = transaction
                )
            )


        AppLogger.debug("void response: $refundResponse")
        return refundResponse
}
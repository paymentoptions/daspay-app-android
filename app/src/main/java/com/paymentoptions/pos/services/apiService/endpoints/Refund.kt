package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.RefundResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generateRefundRequestHeader

suspend fun refund(
    context: Context,
    transactionId: String,
    merchantId: String,
    amount: String,
    notes: String?,
): RefundResponse? {

        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded() ?: return null

        val idToken = authDetails.data.token.idToken
        val requestHeaders = generateRefundRequestHeader(idToken)

        //AppLogger.debug("refund request: $transaction | $authDetails")
        val refundResponse: RefundResponse =
            RetrofitClient.getApi(context).refund(
                headers = requestHeaders,
                request = com.paymentoptions.pos.services.apiService.TransactionRequest(
                    transactionId = transactionId,
                    merchant_id = merchantId,
                   // daspay_res = transaction,
                    amount = amount,
                    notes = if(notes?.isBlank() == true) "Refund from DASPay App" else notes!!
                )
            )


        AppLogger.debug("refund response: $refundResponse")
        return refundResponse
}
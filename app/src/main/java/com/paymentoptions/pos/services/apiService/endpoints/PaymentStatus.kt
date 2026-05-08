package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.PaymentStatusRequest
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.generatePaymentStatusHeader
import com.theminesec.lib.dto.transaction.TranStatus

suspend fun paymentStatus(
    context: Context,
    request: PaymentStatusRequest,
    tranStatus: TranStatus,
): Boolean {
    try {
        val requestHeaders = generatePaymentStatusHeader()

        AppLogger.debug("inThis PaymentStatus request -->: $request")
        val response: String =
            RetrofitClient.getApi(context).paymentStatus(headers = requestHeaders, request = request)

        AppLogger.debug("inThis PaymentStatus response -->: $response")
    } catch (e: Exception) {
        AppLogger.debug("inThis PaymentStatus error: ${e.stackTrace}")
        throw e
    }
    AppLogger.debug("inThis PaymentStatus tranStatus -->: $tranStatus")
    return tranStatus == TranStatus.APPROVED
}
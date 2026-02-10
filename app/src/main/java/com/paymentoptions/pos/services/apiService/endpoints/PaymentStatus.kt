package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.PaymentStatusRequest
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generatePaymentStatusHeader

suspend fun paymentStatus(
    context: Context,
    request: PaymentStatusRequest,
): Boolean {
    try {
        val requestHeaders = generatePaymentStatusHeader()

        AppLogger.debug("inThis PaymentStatus request -->: $request")
        val response: String =
            RetrofitClient.getApi(context).paymentStatus(headers = requestHeaders, request = request)

        AppLogger.debug("inThis PaymentStatus response -->: $response")

        return response.uppercase() == "SUCCESS"
    } catch (e: Exception) {
        AppLogger.debug("paymentStatusError: ${e.stackTrace}")
        throw e
    }
}
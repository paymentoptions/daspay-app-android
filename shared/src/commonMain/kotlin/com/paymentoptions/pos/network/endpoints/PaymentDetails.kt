package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.PaymentDetailsResponse
import com.paymentoptions.pos.network.applyDaspayHeaders
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.get

suspend fun paymentDetails(paymentId: String): PaymentDetailsResponse? {
    val response = KtorClient.instance.get(ConfigurationManager.url(ApiEndpoints.PAYMENT_DETAILS + paymentId)) {
        applyDaspayHeaders()
    }
    response.throwIfNotSuccess("paymentDetails")
    return response.body<PaymentDetailsResponse>()
}

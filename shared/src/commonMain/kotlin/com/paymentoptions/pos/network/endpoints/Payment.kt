package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.PaymentRequest
import com.paymentoptions.pos.network.PaymentResponse
import com.paymentoptions.pos.network.applyPaymentRequestHeader
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

suspend fun payment(request: PaymentRequest): PaymentResponse? {
    val response = KtorClient.instance.post(ConfigurationManager.url(ApiEndpoints.PAYMENT)) {
        contentType(ContentType.Application.Json)
        applyPaymentRequestHeader()
        setBody(request)
    }
    response.throwIfNotSuccess("payment")
    return response.body<PaymentResponse>()
}

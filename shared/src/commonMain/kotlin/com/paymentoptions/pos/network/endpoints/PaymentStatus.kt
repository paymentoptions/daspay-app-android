package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.PaymentStatusRequest
import com.paymentoptions.pos.network.applyPaymentStatusHeader
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

suspend fun paymentStatus(request: PaymentStatusRequest): String? {
    val response = KtorClient.instance.post(ConfigurationManager.url(ApiEndpoints.PAYMENT_STATUS)) {
        contentType(ContentType.Application.Json)
        applyPaymentStatusHeader()
        setBody(request)
    }
    response.throwIfNotSuccess("paymentStatus")
    return response.body<String>()
}

package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.RefundResponse
import com.paymentoptions.pos.network.TransactionRequest
import com.paymentoptions.pos.network.applyDaspayHeaders
import com.paymentoptions.pos.network.applyRefundRequestHeader
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

suspend fun refund(
    transactionId: String,
    merchantId: String,
    amount: String? = null,
    notes: String? = null,
): RefundResponse? {
    val response = KtorClient.instance.post(ConfigurationManager.url(ApiEndpoints.REFUND)) {
        contentType(ContentType.Application.Json)
        applyRefundRequestHeader()
        setBody(TransactionRequest(transactionId, merchantId, amount, notes))
    }
    response.throwIfNotSuccess("refund")
    return response.body<RefundResponse>()
}

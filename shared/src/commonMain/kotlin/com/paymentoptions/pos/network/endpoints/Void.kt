package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.RefundResponse
import com.paymentoptions.pos.network.TransactionRequest
import com.paymentoptions.pos.network.applyDaspayHeaders
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

suspend fun void(
    transactionId: String,
    merchantId: String,
    notes: String? = null,
): RefundResponse? {
    val response = KtorClient.instance.post(ConfigurationManager.url(ApiEndpoints.VOID)) {
        contentType(ContentType.Application.Json)
        applyDaspayHeaders()
        setBody(TransactionRequest(transactionId, merchantId, notes = notes))
    }
    response.throwIfNotSuccess("void")
    return response.body<RefundResponse>()
}

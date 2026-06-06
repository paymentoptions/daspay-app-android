package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.SettleBatchRequest
import com.paymentoptions.pos.network.SettleBatchResponse
import com.paymentoptions.pos.network.applyPaymentRequestHeader
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

suspend fun settleBatch(batchId: String): SettleBatchResponse? {
    val response = KtorClient.instance.post(ConfigurationManager.url(ApiEndpoints.SETTLE_BATCH)) {
        contentType(ContentType.Application.Json)
        applyPaymentRequestHeader()
        setBody(SettleBatchRequest(batchId))
    }
    response.throwIfNotSuccess("settleBatch")
    return response.body<SettleBatchResponse>()
}

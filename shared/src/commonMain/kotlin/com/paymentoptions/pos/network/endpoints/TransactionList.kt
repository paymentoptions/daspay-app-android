package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.TransactionListResponse
import com.paymentoptions.pos.network.TransactionListV2Request
import com.paymentoptions.pos.network.applyDaspayHeaders
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

suspend fun transactionList(take: Int, skip: Int): TransactionListResponse? {
    val response = KtorClient.instance.get(ConfigurationManager.url(ApiEndpoints.TRANSACTION_LIST)) {
        applyDaspayHeaders()
        parameter("take", take)
        parameter("skip", skip)
    }
    response.throwIfNotSuccess("transactionList")
    return response.body<TransactionListResponse>()
}

suspend fun transactionListV2(request: TransactionListV2Request): TransactionListResponse? {
    val response = KtorClient.instance.post(ConfigurationManager.url(ApiEndpoints.TRANSACTION_LIST_V2)) {
        contentType(ContentType.Application.Json)
        applyDaspayHeaders()
        setBody(request)
    }
    response.throwIfNotSuccess("transactionListV2")
    return response.body<TransactionListResponse>()
}

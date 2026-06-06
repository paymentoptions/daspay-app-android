package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.StatsV2Request
import com.paymentoptions.pos.network.StatsV2Response
import com.paymentoptions.pos.network.applyRefundRequestHeader
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

suspend fun statsV2(request: StatsV2Request): StatsV2Response? {
    val response = KtorClient.instance.post(ConfigurationManager.url(ApiEndpoints.STATS_V2)) {
        contentType(ContentType.Application.Json)
        applyRefundRequestHeader()
        setBody(request)
    }
    response.throwIfNotSuccess("statsV2")
    return response.body<StatsV2Response>()
}

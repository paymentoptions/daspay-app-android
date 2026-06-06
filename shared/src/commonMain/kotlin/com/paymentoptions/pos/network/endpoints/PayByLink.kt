package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.PayByLinkRequest
import com.paymentoptions.pos.network.PayByLinkResponse
import com.paymentoptions.pos.network.applyDaspayHeaders
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

suspend fun payByLink(dasmid: String, request: PayByLinkRequest): PayByLinkResponse? {
    val response = KtorClient.instance.post(ConfigurationManager.url(ApiEndpoints.PAY_BY_LINK + dasmid)) {
        contentType(ContentType.Application.Json)
        applyDaspayHeaders()
        parameter("dasmid", dasmid)
        setBody(request)
    }
    response.throwIfNotSuccess("payByLink")
    return response.body<PayByLinkResponse>()
}

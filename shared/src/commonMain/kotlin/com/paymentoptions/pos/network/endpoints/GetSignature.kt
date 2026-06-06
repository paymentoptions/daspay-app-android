package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.GetSignatureResponse
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.applyDaspayHeaders
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType

suspend fun getSignature(
    uuid: String? = null,
): GetSignatureResponse? {
    val response = KtorClient.instance.get(ConfigurationManager.url(ApiEndpoints.GET_SIGNATURE + uuid)) {
        contentType(ContentType.Application.Json)
        applyDaspayHeaders()
    }
    response.throwIfNotSuccess("getSignature")
    return response.body<GetSignatureResponse>()
}

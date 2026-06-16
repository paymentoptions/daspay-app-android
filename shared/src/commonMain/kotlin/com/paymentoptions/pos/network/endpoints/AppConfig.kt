package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.AppConfigResponse
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.applyDaspayHeaders
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

suspend fun getAppConfiguration(flavourName: String): AppConfigResponse? {
    val response = KtorClient.instance.get(ConfigurationManager.url(ApiEndpoints.APP_CONFIGURATION)) {
        parameter("flavour", flavourName)
        applyDaspayHeaders()
    }
    response.throwIfNotSuccess("getAppConfiguration")
    return response.body<AppConfigResponse>()
}

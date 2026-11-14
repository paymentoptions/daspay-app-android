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

/**
 * Downloads the app config for [environment] (DEV / UAT / PROD).
 *
 * Always calls the flavour's bootstrap URL, never the saved config base URL, so a bad saved URL
 * can't stop the app from fetching a corrected config. Needs a signed-in user: KtorClient's bearer
 * auth attaches the ID token and refreshes it on 401.
 */
suspend fun getAppConfiguration(environment: String): AppConfigResponse? {
    val url = "${ConfigurationManager.bootstrapBaseUrl.trimEnd('/')}/${ApiEndpoints.APP_CONFIGURATION.trimStart('/')}"
    val response = KtorClient.instance.get(url) {
        parameter("appenv", environment)
        applyDaspayHeaders()
    }
    response.throwIfNotSuccess("getAppConfiguration")
    return response.body<AppConfigResponse>()
}

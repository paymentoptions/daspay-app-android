package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.SignInRequest
import com.paymentoptions.pos.network.SignInResponse
import com.paymentoptions.pos.network.applyDaspaySignHeaders
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

suspend fun signIn(username: String, password: String, deviceNumber: String): SignInResponse? {
    val response = KtorClient.instance.post(ConfigurationManager.url(ApiEndpoints.SIGN_IN)) {
        contentType(ContentType.Application.Json)
        applyDaspaySignHeaders(true, deviceNumber)
        setBody(SignInRequest(username, password))
    }
    response.throwIfNotSuccess("signIn")
    return response.body<SignInResponse>()
}

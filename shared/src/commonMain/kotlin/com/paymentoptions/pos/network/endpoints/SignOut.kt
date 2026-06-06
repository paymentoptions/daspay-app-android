package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.SignOutRequest
import com.paymentoptions.pos.network.SignOutResponse
import com.paymentoptions.pos.network.Token
import com.paymentoptions.pos.network.applyDaspayHeaders
import com.paymentoptions.pos.network.throwIfNotSuccess
import com.paymentoptions.pos.storage.AppStorage
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

suspend fun signOut(): SignOutResponse? {
    val username = AppStorage.userEmail ?: return null
    val token = Token(
        accessToken  = AppStorage.accessToken  ?: "",
        idToken      = AppStorage.idToken      ?: "",
        refreshToken = AppStorage.refreshToken ?: "",
    )
    val response = KtorClient.instance.post(ConfigurationManager.url(ApiEndpoints.SIGN_OUT)) {
        contentType(ContentType.Application.Json)
        applyDaspayHeaders()
        setBody(SignOutRequest(username, token))
    }
    response.throwIfNotSuccess("signOut")
    return response.body<SignOutResponse>()
}

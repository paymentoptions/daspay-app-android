package com.paymentoptions.pos.services.apiService.endpoints

import com.paymentoptions.pos.services.apiService.RefreshTokenRequest
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.generateRequestHeaders
import kotlinx.coroutines.runBlocking

fun refreshTokens(username: String = "", refreshToken: String = "") = runBlocking {
    try {
        val requestHeaders = generateRequestHeaders()
        val refreshTokenRequest = RefreshTokenRequest(username, refreshToken)
        val refreshTokenResponse =
            RetrofitClient.api.refreshToken(requestHeaders, refreshTokenRequest)
        return@runBlocking refreshTokenResponse
    } catch (e: Exception) {
        println("refreshTokensError: $e")
        return@runBlocking null
    }
}
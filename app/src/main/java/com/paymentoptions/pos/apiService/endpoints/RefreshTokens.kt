package com.paymentoptions.pos.apiService.endpoints

import com.paymentoptions.pos.apiService.RefreshTokenRequest
import com.paymentoptions.pos.apiService.RetrofitClient
import com.paymentoptions.pos.apiService.generateRequestHeaders
import kotlinx.coroutines.runBlocking

fun refreshTokens(username: String = "", refreshToken: String = "") = runBlocking {

    println("refreshTokens")
    try {
        val requestHeaders = generateRequestHeaders()
        val refreshTokenRequest = RefreshTokenRequest(username, refreshToken)
        val refreshTokenResponse =
            RetrofitClient.api.refreshToken(requestHeaders, refreshTokenRequest)
        return@runBlocking refreshTokenResponse
    } catch (e: Exception) {
        println("refreshTokensError: ${e.message}")
        throw e
    }
}
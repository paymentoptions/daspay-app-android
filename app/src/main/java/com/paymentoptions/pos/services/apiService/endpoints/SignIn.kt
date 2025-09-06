package com.paymentoptions.pos.services.apiService.endpoints

import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.SignInRequest
import com.paymentoptions.pos.services.apiService.SignInResponse
import com.paymentoptions.pos.services.apiService.generateRequestHeaders

suspend fun signIn(username: String, password: String): SignInResponse? {
    try {
        val requestHeaders = generateRequestHeaders()
        val signInRequest = SignInRequest(username, password)
        val signInResponse = RetrofitClient.api.signIn(requestHeaders, signInRequest)
        return signInResponse
    } catch (e: Exception) {
        println("SignInError: ${e.message}")
        throw e
    }
}
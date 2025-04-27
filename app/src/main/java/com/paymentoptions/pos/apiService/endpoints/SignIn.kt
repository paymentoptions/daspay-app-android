package com.paymentoptions.pos.apiService.endpoints

import com.paymentoptions.pos.apiService.RetrofitClient
import com.paymentoptions.pos.apiService.SignInRequest
import com.paymentoptions.pos.apiService.generateRequestHeaders
import kotlinx.coroutines.runBlocking

fun signIn(username: String, password: String) = runBlocking {
    try {
        val requestHeaders = generateRequestHeaders()
        val signInRequest = SignInRequest(username, password)
        val signInResponse = RetrofitClient.api.signIn(requestHeaders, signInRequest)
        return@runBlocking signInResponse
    } catch (e: Exception) {
        println("SignInError: ${e.message}")
        throw e
    }
}
package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.SignInRequest
import com.paymentoptions.pos.services.apiService.SignInResponse
import com.paymentoptions.pos.services.apiService.generateRequestHeader
import com.paymentoptions.pos.services.apiService.generateSignedRequestHeader

suspend fun signIn(context: Context, username: String, password: String): SignInResponse? {
    try {
        val requestHeaders = generateSignedRequestHeader()
        val signInRequest = SignInRequest(username, password)
        val signInResponse = RetrofitClient.getApi(context).signIn(requestHeaders, signInRequest)
        return signInResponse
    } catch (e: Exception) {
        AppLogger.debug("SignInError: ${e.message}")
        throw e
    }
}
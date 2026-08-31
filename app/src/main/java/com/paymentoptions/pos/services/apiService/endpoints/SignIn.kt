package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.SignInRequest
import com.paymentoptions.pos.services.apiService.SignInResponse
import com.paymentoptions.pos.services.apiService.generateRequestHeader
import com.paymentoptions.pos.services.apiService.generateSignedRequestHeader

suspend fun signIn(context: Context, username: String, password: String): SignInResponse? {
    val requestHeaders = generateSignedRequestHeader(context)
    val signInRequest = SignInRequest(username, password)
    val signInResponse = RetrofitClient.getApi(context).signIn(requestHeaders, signInRequest)
    return signInResponse
}
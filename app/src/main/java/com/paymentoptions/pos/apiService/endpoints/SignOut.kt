package com.paymentoptions.pos.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.apiService.RetrofitClient
import com.paymentoptions.pos.apiService.SignOutRequest
import com.paymentoptions.pos.apiService.Token
import com.paymentoptions.pos.apiService.generateRequestHeaders
import com.paymentoptions.pos.apiService.shouldRefreshToken
import com.paymentoptions.pos.device.SharedPreferences
import kotlinx.coroutines.runBlocking

fun signOut(context: Context) = runBlocking {
    try {
        var authDetails = SharedPreferences.getAuthDetails(context)
        val username = authDetails?.data?.email ?: ""
        val accessToken = authDetails?.data?.token?.accessToken ?: ""
        val idToken = authDetails?.data?.token?.idToken ?: ""
        val refreshToken = authDetails?.data?.token?.refreshToken ?: ""
        val shouldRefreshToken = shouldRefreshToken(authDetails)

//        if(shouldRefreshToken){
//            val refreshTokenResponse = refreshTokens(username, refreshToken)
//            SharedPreferences.saveAuthDetails(context, refreshTokenResponse)
//        }

        val requestHeaders = generateRequestHeaders()
        val token = Token(accessToken, idToken, refreshToken)
        val signOutRequest = SignOutRequest(username, token)
        val signOutResponse = RetrofitClient.api.signOut(requestHeaders, signOutRequest)
        return@runBlocking signOutResponse
    } catch (e: Exception) {
        println("SignOutError: ${e.message}")
        throw e
    }
}
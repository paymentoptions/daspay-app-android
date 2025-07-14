package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.SignOutRequest
import com.paymentoptions.pos.services.apiService.SignOutResponse
import com.paymentoptions.pos.services.apiService.Token
import com.paymentoptions.pos.services.apiService.generateRequestHeaders
import com.paymentoptions.pos.services.apiService.shouldRefreshToken

suspend fun signOut(context: Context): SignOutResponse? {
    try {
        var authDetails = SharedPreferences.getAuthDetails(context)
        val username = authDetails?.data?.email ?: ""
        var refreshToken = authDetails?.data?.token?.refreshToken ?: ""
        val shouldRefreshToken = shouldRefreshToken(authDetails?.data?.exp)

        if (shouldRefreshToken) authDetails = refreshTokens(context, username, refreshToken)

        val accessToken = authDetails?.data?.token?.accessToken ?: ""
        val idToken = authDetails?.data?.token?.idToken ?: ""
        refreshToken = authDetails?.data?.token?.refreshToken ?: ""

        val requestHeaders = generateRequestHeaders()
        val token = Token(accessToken, idToken, refreshToken)
        val signOutRequest = SignOutRequest(username, token)

        val signOutResponse = RetrofitClient.api.signOut(requestHeaders, signOutRequest)
        return signOutResponse
    } catch (e: Exception) {
        println("SignOutError: $e")
        throw e
    }
}
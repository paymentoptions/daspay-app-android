package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.RefreshTokenRequest
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.SignInResponse
import com.paymentoptions.pos.services.apiService.generateRequestHeaders

suspend fun refreshTokens(
    context: Context,
    username: String = "",
    refreshToken: String = "",
): SignInResponse? {
    println("---> Refreshing tokens")
    try {
        val requestHeaders = generateRequestHeaders()
        val refreshTokenRequest = RefreshTokenRequest(username, refreshToken)
        val refreshTokenResponse =
            RetrofitClient.api.refreshToken(requestHeaders, refreshTokenRequest)

        SharedPreferences.saveAuthDetails(context, refreshTokenResponse)
        return refreshTokenResponse
    } catch (e: Exception) {
        println("refreshTokensError: $e")
        SharedPreferences.clearSharedPreferences(context)
        return null
    }
}
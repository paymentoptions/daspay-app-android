package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.SignOutRequest
import com.paymentoptions.pos.services.apiService.SignOutResponse
import com.paymentoptions.pos.services.apiService.Token
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generateRequestHeader

suspend fun signOut(context: Context): SignOutResponse? {
    try {
        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded()

        val accessToken = authDetails?.data?.token?.accessToken ?: ""
        val idToken = authDetails?.data?.token?.idToken ?: ""
        val username = authDetails?.data?.email ?: ""
        val refreshToken = authDetails?.data?.token?.refreshToken ?: ""

        val requestHeaders = generateRequestHeader()
        val token = Token(accessToken, idToken, refreshToken)
        val signOutRequest = SignOutRequest(username, token)

        val signOutResponse = RetrofitClient.getApi(context).signOut(requestHeaders, signOutRequest)
        return signOutResponse
    } catch (e: Exception) {
        AppLogger.debug("SignOutError: $e")
        throw e
    }
}
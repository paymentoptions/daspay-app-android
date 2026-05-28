package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.GetSignatureResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generateRequestHeader

suspend fun getSignature(
    context: Context,
    uuid: String,
): GetSignatureResponse? {
    return try {
        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded() ?: return null

        val idToken = authDetails.data.token.idToken
        val requestHeaders = generateRequestHeader(authToken = idToken)

        val response = RetrofitClient.getApi(context).getSignature(
            headers = requestHeaders,
            uuid = uuid
        )

        AppLogger.debug("GetSignature: $response")
        response
    } catch (e: Exception) {
        AppLogger.error("GetSignature error: ${e.message}")
        null
    }
}


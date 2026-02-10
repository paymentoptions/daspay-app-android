package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.services.apiService.PayByLinkRequest
import com.paymentoptions.pos.services.apiService.PayByLinkResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generateRequestHeader

suspend fun payByLink(
    context: Context,
    payByLinkRequest: PayByLinkRequest,
    dasmid: String,
): PayByLinkResponse? {
    try {
        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded() ?: return null

        val idToken = authDetails.data.token.idToken
        val requestHeaders = generateRequestHeader(idToken)

//      val dasmid = getPayByLinkDasmid(context)
        var response: PayByLinkResponse = RetrofitClient.getApi(context).payByLink(
            headers = requestHeaders,
            dasmid = dasmid,
            request = payByLinkRequest
        )

        return response
    } catch (e: Exception) {
        println("payByLinkError: $e")
        throw e
    }
}
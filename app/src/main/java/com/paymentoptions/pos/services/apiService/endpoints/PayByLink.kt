package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.device.getDasmid
import com.paymentoptions.pos.services.apiService.PayByLinkRequest
import com.paymentoptions.pos.services.apiService.PayByLinkResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.generateRequestHeaders
import com.paymentoptions.pos.services.apiService.shouldRefreshToken

suspend fun payByLink(
    context: Context,
    payByLinkRequest: PayByLinkRequest,
): PayByLinkResponse? {
    try {
        var authDetails = SharedPreferences.getAuthDetails(context)
        val username = authDetails?.data?.email ?: ""
        val refreshToken = authDetails?.data?.token?.refreshToken ?: ""
        val shouldRefreshToken = shouldRefreshToken(authDetails?.data?.exp)

        if (shouldRefreshToken) authDetails = refreshTokens(context, username, refreshToken)

        val idToken = authDetails?.data?.token?.idToken
        val requestHeaders = generateRequestHeaders(idToken ?: "")

        val dasmid = getDasmid(context)

        println("payByLink: $payByLinkRequest")
        var response: PayByLinkResponse = RetrofitClient.api.payByLink(
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
package com.paymentoptions.pos.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.apiService.RefundRequest
import com.paymentoptions.pos.apiService.RetrofitClient
import com.paymentoptions.pos.apiService.SignInResponse
import com.paymentoptions.pos.apiService.generateRequestHeaders
import com.paymentoptions.pos.apiService.shouldRefreshToken
import com.paymentoptions.pos.device.SharedPreferences
import kotlinx.coroutines.runBlocking

fun refund(
    context: Context,
    transactionId: String,
    merchantId: String,
    amount: Float,
    notes: String = "",
) = runBlocking {
    try {
        var authDetails = SharedPreferences.getAuthDetails(context)
        val username = authDetails?.data?.email
        val refreshToken = authDetails?.data?.token?.refreshToken
        val shouldRefreshToken = shouldRefreshToken(authDetails)
        var refreshTokenResponse: SignInResponse? = authDetails

//        if(shouldRefreshToken){
//            refreshTokenResponse = refreshTokens(username ?: "", refreshToken ?: "")
//            SharedPreferences.saveAuthDetails(context, refreshTokenResponse)
//        }
//        authDetails = SharedPreferences.getAuthDetails(context)

        val requestHeaders =
            generateRequestHeaders(refreshTokenResponse?.data?.token?.idToken ?: "")
        val refundRequest = RefundRequest(transactionId, merchantId, amount, notes)
        val refundResponse = RetrofitClient.api.refund(requestHeaders, refundRequest)
        return@runBlocking refundResponse
    } catch (e: Exception) {
        println("SignInError: ${e.message}")
        throw e
    }
}
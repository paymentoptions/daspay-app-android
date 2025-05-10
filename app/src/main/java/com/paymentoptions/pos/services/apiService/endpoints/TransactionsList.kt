package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TransactionListResponse
import com.paymentoptions.pos.services.apiService.generateRequestHeaders
import com.paymentoptions.pos.services.apiService.shouldRefreshToken

suspend fun transactionsList(
    context: Context,
    take: Int = 10,
    skip: Int = 0,
): TransactionListResponse? {
    try {
        var authDetails = SharedPreferences.getAuthDetails(context)
        val username = authDetails?.data?.email ?: ""
        val refreshToken = authDetails?.data?.token?.refreshToken ?: ""
        val shouldRefreshToken = shouldRefreshToken(authDetails)

        if (shouldRefreshToken) {
            val refreshTokenResponse = refreshTokens(username, refreshToken)
            if (refreshTokenResponse == null) {
                SharedPreferences.clearSharedPreferences(context)
                return null
            } else
                SharedPreferences.saveAuthDetails(context, refreshTokenResponse)
        }

        authDetails = SharedPreferences.getAuthDetails(context)
        val idToken = authDetails?.data?.token?.idToken
        val requestHeaders = generateRequestHeaders(idToken ?: "")

        val transactionListResponse =
            RetrofitClient.api.transactionsList(requestHeaders, take, skip)
        println("transactionListResponse: $transactionListResponse")

        return transactionListResponse
    } catch (e: Exception) {
        println("transactionListError: $e")
        throw e
    }
}
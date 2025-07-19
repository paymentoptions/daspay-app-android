package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.ProductListResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.generateRequestHeaders
import com.paymentoptions.pos.services.apiService.shouldRefreshToken

suspend fun productList(context: Context): ProductListResponse? {
    try {
        var authDetails = SharedPreferences.getAuthDetails(context)
        val username = authDetails?.data?.email ?: ""
        val refreshToken = authDetails?.data?.token?.refreshToken ?: ""
        val shouldRefreshToken = shouldRefreshToken(authDetails?.data?.exp)

        if (shouldRefreshToken) authDetails = refreshTokens(context, username, refreshToken)

        val idToken = authDetails?.data?.token?.idToken
        val requestHeaders = generateRequestHeaders(idToken ?: "")

        val productListResponse = RetrofitClient.api.productList(requestHeaders)

        return productListResponse
    } catch (e: Exception) {
        println("transactionListError: $e")
        throw e
    }
}
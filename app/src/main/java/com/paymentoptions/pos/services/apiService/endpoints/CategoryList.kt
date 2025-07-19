package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.CategoryListResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.generateRequestHeaders
import com.paymentoptions.pos.services.apiService.shouldRefreshToken

suspend fun categoryList(context: Context): CategoryListResponse? {
    try {
        var authDetails = SharedPreferences.getAuthDetails(context)
        val username = authDetails?.data?.email ?: ""
        val refreshToken = authDetails?.data?.token?.refreshToken ?: ""
        val shouldRefreshToken = shouldRefreshToken(authDetails?.data?.exp)

        if (shouldRefreshToken) authDetails = refreshTokens(context, username, refreshToken)

        val idToken = authDetails?.data?.token?.idToken
        val requestHeaders = generateRequestHeaders(idToken ?: "")

        val categoryListResponse = RetrofitClient.api.categoryList(requestHeaders)

        return categoryListResponse
    } catch (e: Exception) {
        println("transactionListError: $e")
        throw e
    }
}
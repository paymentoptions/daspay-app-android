package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.ProductListResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generateRequestHeader

suspend fun productList(context: Context, categoryId: String): ProductListResponse? {
    try {
        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded() ?: return null

        val idToken = authDetails.data.token.idToken
        val requestHeaders = generateRequestHeader(idToken)

        val productListResponse =
            RetrofitClient.getApi(context).productList(headers = requestHeaders, categoryId = categoryId)

        return productListResponse
    } catch (e: Exception) {
        AppLogger.debug("Product List Error: $e")
        throw e
    }
}
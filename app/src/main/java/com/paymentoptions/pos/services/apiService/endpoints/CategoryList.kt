package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.services.apiService.CategoryListResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generateRequestHeader
import com.paymentoptions.pos.utils.decodeJwtPayload
import com.paymentoptions.pos.utils.getMerchantIdFromToken

suspend fun categoryList(context: Context): CategoryListResponse? {
    try {
        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded() ?: return null

        val idToken = authDetails?.data?.token?.idToken
        val requestHeaders = generateRequestHeader(idToken ?: "")

        val decodedJwtPayloadJson = decodeJwtPayload(idToken ?: "")
        val merchantId = getMerchantIdFromToken(decodedJwtPayloadJson)

        val categoryListResponse =
            RetrofitClient.getApi(context).categoryList(headers = requestHeaders, merchantId = merchantId)

        return categoryListResponse
    } catch (e: Exception) {
        println("transactionListError: $e")
        throw e
    }
}
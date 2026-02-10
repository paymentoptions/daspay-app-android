package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.TransactionListResponse
import com.paymentoptions.pos.services.apiService.generateRequestHeader

// Deprecated in favor of TransactionListV2 -----------------------------------

suspend fun transactionList(
    context: Context,
    take: Int = 10,
    skip: Int = 0,
): TransactionListResponse? {
    try {
        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded() ?: return null

        val idToken = authDetails.data.token.idToken
        val requestHeaders = generateRequestHeader(idToken)

        val transactionListResponse = RetrofitClient.getApi(context).transactionList(requestHeaders, take, skip)

        return transactionListResponse
    } catch (e: Exception) {
        println("transactionListError: $e")
        throw e
    }
}
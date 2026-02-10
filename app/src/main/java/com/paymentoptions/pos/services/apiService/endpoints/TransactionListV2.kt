package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.TransactionListResponse
import com.paymentoptions.pos.services.apiService.TransactionListV2Request
import com.paymentoptions.pos.services.apiService.TransactionListV2RequestFilter
import com.paymentoptions.pos.services.apiService.generateRequestHeader

suspend fun transactionListV2(
    context: Context,
    take: Int = 10,
    skip: Int = 0,
    filter: List<TransactionListV2RequestFilter> = listOf(),
): TransactionListResponse? {
    try {
        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded() ?: return null

        val idToken = authDetails.data.token.idToken
        val requestHeaders = generateRequestHeader(idToken)

        val request = TransactionListV2Request(
            take = take,
            skip = skip,
            filter = filter,
            totalRequired = true
        )
        val transactionListResponse = RetrofitClient.getApi(context).transactionListV2(requestHeaders, request)

        return transactionListResponse
    } catch (e: Exception) {
        AppLogger.debug("transactionListError: $e")
        throw e
    }
}
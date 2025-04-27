package com.paymentoptions.pos.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.apiService.RetrofitClient
import com.paymentoptions.pos.apiService.TransactionListResponse
import com.paymentoptions.pos.apiService.generateRequestHeaders
import com.paymentoptions.pos.apiService.shouldRefreshToken
import com.paymentoptions.pos.device.SharedPreferences
import kotlinx.coroutines.runBlocking

fun transactionsList(context: Context, take: Int = 10, skip: Int = 0): TransactionListResponse =
    runBlocking {

        try {
            var authDetails = SharedPreferences.getAuthDetails(context)
            val username = authDetails?.data?.email ?: ""
            val refreshToken = authDetails?.data?.token?.refreshToken ?: ""
            val shouldRefreshToken = shouldRefreshToken(authDetails)

//        if(shouldRefreshToken){
//            val refreshTokenResponse = refreshTokens(username, refreshToken)
//            SharedPreferences.saveAuthDetails(context, refreshTokenResponse)
//        }

//        authDetails = SharedPreferences.getAuthDetails(context)
            val idToken = authDetails?.data?.token?.idToken
            val requestHeaders = generateRequestHeaders(idToken ?: "")

            val transactionListResponse =
                RetrofitClient.api.transactionsList(requestHeaders, take, skip)
            println("transactionListResponse: $transactionListResponse")

            return@runBlocking transactionListResponse
        } catch (e: Exception) {
            println("transactionListError: ${e.message}")
            throw e
        }
    }
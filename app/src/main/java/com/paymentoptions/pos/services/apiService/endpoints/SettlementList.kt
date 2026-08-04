package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.DPSharedPreferences
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.SettlementListResponse
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generatePaymentRequestHeader
import com.paymentoptions.pos.utils.getDeviceIdentifier

suspend fun settlementList(
        context: Context,
    ): SettlementListResponse? {
        try {
            val tokenRepository = TokenRepository.getInstance(context)
            val authDetails = tokenRepository.refreshTokenIfNeeded() ?: return null

            val idToken = authDetails.data.token.idToken
            val requestHeaders = generatePaymentRequestHeader(idToken)

            // Get device information from saved credentials
            val (_, _, otp) = DPSharedPreferences.getSavedCredentials(context)
            val deviceNumber = getDeviceIdentifier(context)
            val uniqueCode = otp ?: ""

            if (uniqueCode.isEmpty()) {
                AppLogger.error("settlementList: UniqueCode (OTP) not found")
                return null
            }

            val settlementListResponse: SettlementListResponse =
                RetrofitClient.getApi(context).getSettlementList(
                    headers = requestHeaders,
                    deviceNumber = deviceNumber,
                    uniqueCode = uniqueCode
                )

            return settlementListResponse
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            AppLogger.error("settlementList HTTP error ${e.code()}: $errorBody")
            return null
        } catch (e: Exception) {
            AppLogger.error("settlementList Error: $e")
            return null
        }
    }
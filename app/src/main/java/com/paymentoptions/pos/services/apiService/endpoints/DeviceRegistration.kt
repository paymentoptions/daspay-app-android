package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.DPSharedPreferences
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.CompleteDeviceRegistrationRequest
import com.paymentoptions.pos.services.apiService.CompleteDeviceRegistrationResponse
import com.paymentoptions.pos.services.apiService.DeviceMetadata
import com.paymentoptions.pos.services.apiService.ExternalConfigurationResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generateRequestHeader
import com.paymentoptions.pos.utils.getDeviceIdentifier


suspend fun completeDeviceRegistration(
    context: Context,
    otp: String
): Result<CompleteDeviceRegistrationResponse> {
    return try {
        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded()

        val idToken = authDetails?.data?.token?.idToken ?: ""
        val requestHeaders = generateRequestHeader(idToken)

        val deviceNumber = getDeviceIdentifier(context)
        val uniqueCode = otp // The static unique code
        val deviceMetadata = DeviceMetadata(
            os = "Android",
            version = android.os.Build.VERSION.RELEASE,
            manufacturer = android.os.Build.MANUFACTURER
        )

        val requestBody = CompleteDeviceRegistrationRequest(
            UniqueCode = uniqueCode,
            DeviceNumber = deviceNumber,
            DeviceMetadata = deviceMetadata
        )

        val response = RetrofitClient.getApi(context).completeDeviceRegistration(requestHeaders, requestBody)

        Result.success(response)

    } catch (e: retrofit2.HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        //if the error is the specific "Device already registered" case
        android.util.Log.e("API_ERROR_RESPONSE", "HTTP ${e.code()}: $errorBody")
        Result.failure(Exception(errorBody))

    } catch (e: Exception) {
        android.util.Log.e("API_ERROR_RESPONSE", "A general error occurred", e)
        Result.failure(e)
    }
}

suspend fun getExternalDeviceConfiguration(
    context: Context,
    otp: String
): Result<ExternalConfigurationResponse> {
    return try {
        val deviceNumber = getDeviceIdentifier(context)
        val uniqueCode = otp

        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded()

        val idToken = authDetails?.data?.token?.idToken ?: ""
        val requestHeaders = generateRequestHeader(idToken)

        //Log.d("Request Headers-->", "$requestHeaders->$deviceNumber->$uniqueCode")

        // The function name here is now corrected
        val response = RetrofitClient.getApi(context).getDeviceConfiguration(requestHeaders, deviceNumber, uniqueCode)
        Result.success(response)
    } catch (e: retrofit2.HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        val serverMessage = try {
            val json = org.json.JSONObject(errorBody ?: "")
            json.optString("message", e.message())
        } catch (_: Exception) {
            e.message()
        }
        AppLogger.error("GetExternalDeviceConfigurationError: $serverMessage")
        Result.failure(Exception(serverMessage))
    } catch (e: Exception) {
        AppLogger.error("GetExternalDeviceConfigurationError: ${e.message}")
        Result.failure(e)
    }
}
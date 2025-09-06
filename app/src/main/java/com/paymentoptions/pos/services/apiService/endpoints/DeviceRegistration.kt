package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.*
import com.paymentoptions.pos.utils.getDeviceIdentifier


suspend fun completeDeviceRegistration(
    context: Context,
    otp: String
): Result<CompleteDeviceRegistrationResponse> {
    return try {
        var authDetails = SharedPreferences.getAuthDetails(context)
        val username = authDetails?.data?.email ?: ""
        val refreshToken = authDetails?.data?.token?.refreshToken ?: ""

        if (shouldRefreshToken(authDetails?.data?.exp)) {
            authDetails = refreshTokens(context, username, refreshToken)
        }

        val idToken = authDetails?.data?.token?.idToken ?: ""
        val requestHeaders = generateRequestHeaders(idToken)


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

        val response = RetrofitClient.api.completeDeviceRegistration(requestHeaders, requestBody)

        Result.success(response)

    } catch (e: retrofit2.HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        //if the error is the specific "Device already registered" case
        android.util.Log.e("API_ERROR_RESPONSE", "HTTP ${e.code()}: $errorBody")
        Result.failure(Exception("HTTP ${e.code()}: $errorBody"))

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

        var authDetails = SharedPreferences.getAuthDetails(context)
        val username = authDetails?.data?.email ?: ""
        val refreshToken = authDetails?.data?.token?.refreshToken ?: ""

        if (shouldRefreshToken(authDetails?.data?.exp)) {
            // re-assign authDetails after refreshing the token
            authDetails = refreshTokens(context, username, refreshToken)
        }

        val idToken = authDetails?.data?.token?.idToken ?: ""
        val requestHeaders = generateRequestHeaders(idToken)

        //Log.d("Request Headers-->", "$requestHeaders->$deviceNumber->$uniqueCode")

        // The function name here is now corrected
        val response = RetrofitClient.api.getDeviceConfiguration(requestHeaders, deviceNumber, uniqueCode)
        Result.success(response)
    } catch (e: Exception) {
        println("GetExternalDeviceConfigurationError: ${e.message}")
        Result.failure(e)
    }
}
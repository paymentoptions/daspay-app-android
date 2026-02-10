package com.paymentoptions.pos.services.apiService

import android.content.Context
import com.google.gson.Gson
import com.paymentoptions.pos.device.SharedPreferences
import retrofit2.HttpException

/**
 * Error response model for API errors
 */
data class ApiErrorResponse(
    val status: Int? = null,
    val success: Boolean? = null,
    val message: String? = null,
    val messageCode: String? = null
)

class TokenRepository private constructor(
    private val context: Context
) {

    private var isRefreshing = false
    private var renewCount = 0

    suspend fun doRefreshToken(authDetails: SignInResponse): SignInResponse? {
//        val authDetails = getAuthToken() ?: return null
//
//        if (!shouldRefreshToken(authDetails.data.exp)) {
//            println("TokenRepository using existing token 2")
//            return authDetails
//        }

        try {
            println("TokenRepository refreshing token 4, renewCount $renewCount")
            isRefreshing = true
            renewCount ++
            val username = authDetails.data.email
            val refreshToken = authDetails.data.token.refreshToken
            val requestHeaders = generateRequestHeader()
            val refreshTokenRequest = RefreshTokenRequest(username, refreshToken)
            val refreshTokenResponse =
                RetrofitClient.getApi(context).refreshToken(requestHeaders, refreshTokenRequest)
            SharedPreferences.saveAuthDetails(context, refreshTokenResponse)
            isRefreshing = false
            return refreshTokenResponse
        } catch (e: HttpException) {
            println("TokenRepository refresh failed with HTTP error: ${e.code()}")
            isRefreshing = false

            // Try to parse error response
            val errorBody = e.response()?.errorBody()?.string()
            println("TokenRepository error body: $errorBody")

            try {
                val errorResponse = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                if (errorResponse?.messageCode == "ERR_AUTH_0003") {
                    // Refresh token expired - need to re-authenticate
                    println("TokenRepository: Refresh token expired (ERR_AUTH_0003), triggering re-authentication")
                    AuthEventManager.onRefreshTokenExpired()
                }
            } catch (parseException: Exception) {
                println("TokenRepository: Failed to parse error response: ${parseException.message}")
            }
        } catch (e: Exception) {
            println("TokenRepository refresh failed: $e")
            isRefreshing = false

        }
        return null
    }

    suspend fun refreshTokenIfNeeded(): SignInResponse? {
        val authDetails = getAuthToken()
        if(authDetails == null || !shouldRefreshToken(authDetails?.data?.exp)){
            println("TokenRepository going with existing token 1")
            return authDetails
        }
        return doRefreshToken(authDetails)
    }

    fun getAuthToken(): SignInResponse? = SharedPreferences.getAuthDetails(context)

    companion object {
        const val REFRESH_JOB = "token_refresh"
        @Volatile
        private var INSTANCE: TokenRepository? = null

        fun getInstance(context: Context): TokenRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenRepository(context).also { INSTANCE = it }
            }
    }
}

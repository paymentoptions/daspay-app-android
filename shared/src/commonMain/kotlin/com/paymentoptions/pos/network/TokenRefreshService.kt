package com.paymentoptions.pos.network

import com.paymentoptions.pos.platformLog
import com.paymentoptions.pos.platformLogError
import com.paymentoptions.pos.storage.AppStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Refreshes the access token using the stored refresh token.
 * Returns a [BearerTokens] pair on success, null on failure.
 */
object TokenRefreshService {

    suspend fun refreshToken(): BearerTokens? {
        return try {
            val username = AppStorage.userEmail ?: return null
            val refreshToken = AppStorage.refreshToken ?: return null

            // Use a fresh client without auth interceptor to avoid circular refresh
            val client = HttpClient {
                install(ContentNegotiation) {
                    json(Json {
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) = platformLog("refreshToken", message)
                    }
                    level = LogLevel.ALL
                }
            }
            val response = try {
                client.post(ConfigurationManager.url(ApiEndpoints.REFRESH_TOKEN)) {
                    contentType(ContentType.Application.Json)
                    setBody(RefreshTokenRequest(username, refreshToken))
                    applyDaspaySignHeaders(true,AppStorage.deviceNumber?:"")
                }
            } finally {
                client.close()
            }

            val signIn = response.body<SignInResponse>()
            val data = signIn.data
            if (signIn.success == true && data != null) {
                platformLog("TokenRefresh", "Token refreshed successfully")
                AppStorage.accessToken  = data.token.accessToken
                AppStorage.idToken      = data.token.idToken
                AppStorage.refreshToken = data.token.refreshToken
                AppStorage.tokenExpiry  = data.exp * 1000L
                BearerTokens(data.token.idToken, data.token.refreshToken)
            } else null
        } catch (e: Exception) {
            platformLogError("TokenRefresh", "Token refresh failed", e)
            null
        }
    }
}

package com.paymentoptions.pos.network

import com.paymentoptions.pos.platformLog
import com.paymentoptions.pos.platformLogError
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.utils.decodeJwtPayload
import com.paymentoptions.pos.utils.getDeviceIdentifier
import com.paymentoptions.pos.utils.isAndroid
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/** Outcome of a token refresh. */
sealed interface TokenRefreshResult {
    data class Success(val tokens: BearerTokens) : TokenRefreshResult

    /** The server refused the refresh token (HTTP 401/403 or ERR_AUTH_0003): the session is over. */
    data object AuthRejected : TokenRefreshResult

    /** Anything else (network error, unexpected response); the session may still be valid. */
    data object Failed : TokenRefreshResult
}

/**
 * Refreshes the access token using the stored refresh token.
 */
object TokenRefreshService {

    private const val REFRESH_TOKEN_EXPIRED_CODE = "ERR_AUTH_0003"

    /** Returns a [BearerTokens] pair on success, null on any failure. */
    suspend fun refreshToken(): BearerTokens? = (refresh() as? TokenRefreshResult.Success)?.tokens

    suspend fun refresh(): TokenRefreshResult {
        return try {
            val username = AppStorage.userEmail ?: return TokenRefreshResult.Failed
            val refreshToken = AppStorage.refreshToken ?: return TokenRefreshResult.Failed

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
                    // Same device ID that sign-in sends in x-domain.
                    applyDaspaySignHeaders(isAndroid(), getDeviceIdentifier())
                }
            } finally {
                client.close()
            }

            val status = response.status.value
            if (status == 401 || status == 403) {
                platformLog("TokenRefresh", "Refresh token rejected (HTTP $status)")
                return TokenRefreshResult.AuthRejected
            }
            if (!response.status.isSuccess()) {
                val messageCode = runCatching {
                    Json.parseToJsonElement(response.bodyAsText()).jsonObject["messageCode"]?.jsonPrimitive?.contentOrNull
                }.getOrNull()
                platformLog("TokenRefresh", "Refresh failed (HTTP $status, messageCode=$messageCode)")
                return if (messageCode == REFRESH_TOKEN_EXPIRED_CODE) TokenRefreshResult.AuthRejected
                else TokenRefreshResult.Failed
            }

            val signIn = response.body<SignInResponse>()
            val data = signIn.data
            if (signIn.success == true && data != null) {
                platformLog("TokenRefresh", "Token refreshed successfully")
                AppStorage.accessToken  = data.token.accessToken
                AppStorage.idToken      = data.token.idToken
                AppStorage.refreshToken = data.token.refreshToken
                // The response's top-level exp is not always moved forward on refresh, so prefer
                // the new ID token's own exp claim; otherwise the next proactive cycle comes too early.
                AppStorage.tokenExpiry  = idTokenExpiryMs(data.token.idToken) ?: (data.exp * 1000L)
                TokenRefreshResult.Success(BearerTokens(data.token.idToken, data.token.refreshToken))
            } else TokenRefreshResult.Failed
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            platformLogError("TokenRefresh", "Token refresh failed", e)
            TokenRefreshResult.Failed
        }
    }
}

/** The `exp` claim of a JWT in epoch milliseconds, or null when it can't be read. */
private fun idTokenExpiryMs(idToken: String): Long? = runCatching {
    decodeJwtPayload(idToken)["exp"]?.jsonPrimitive?.longOrNull?.times(1000L)
}.getOrNull()

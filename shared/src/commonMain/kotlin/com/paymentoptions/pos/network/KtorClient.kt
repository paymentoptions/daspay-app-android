package com.paymentoptions.pos.network

import com.paymentoptions.pos.analytics.AnalyticsHelper
import com.paymentoptions.pos.auth.AuthEventManager
import com.paymentoptions.pos.platformLog
import com.paymentoptions.pos.storage.AppStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val TAG = "KtorClient"
private const val TIMEOUT_MS = 30_000L

/**
 * Singleton Ktor [HttpClient].
 *
 * Automatically injects the Bearer token from [AppStorage] and refreshes it
 * via [TokenRefreshService] on 401.  The base URL is read from
 * [ConfigurationManager] on every request so dynamic switching (dev/staging/prod)
 * works without recreating the client.
 */
object KtorClient {

    val instance: HttpClient by lazy { buildClient() }

    private fun buildClient() = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
                coerceInputValues = true
                explicitNulls = false
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = platformLog(TAG, message)
            }
            level = LogLevel.ALL
        }

        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT_MS
            connectTimeoutMillis = TIMEOUT_MS
            socketTimeoutMillis  = TIMEOUT_MS
        }

        // Report every non-2xx response and every failed request to analytics
        // (replaces the OkHttp ApiErrorTrackingInterceptor on release). Only the method, path and
        // status are sent: the body is not read here, so callers can still read it, and error
        // bodies can contain customer data. Doesn't throw, so request behaviour is unchanged.
        HttpResponseValidator {
            validateResponse { response ->
                if (!response.status.isSuccess()) {
                    val request = response.call.request
                    AnalyticsHelper.trackApiError(
                        endpoint = "${request.method.value} ${request.url.encodedPath}",
                        statusCode = response.status.value,
                        message = response.status.description,
                    )
                }
            }
            handleResponseExceptionWithRequest { cause, request ->
                AnalyticsHelper.trackApiError(
                    endpoint = "${request.method.value} ${request.url.encodedPath}",
                    message = cause.message ?: (cause::class.simpleName ?: "Request failed"),
                    throwable = cause,
                )
            }
        }

        install(Auth) {
            bearer {
                loadTokens {
                    platformLog(TAG, "Loading tokens for request")
                    val access  = AppStorage.idToken  ?: return@loadTokens null
                    val refresh = AppStorage.refreshToken ?: return@loadTokens null
                    BearerTokens(access, refresh)
                }
                refreshTokens {
                    platformLog(TAG, "TokenRefresh Refreshing tokens due to 401 response")
                    val refreshed = TokenRefreshService.refreshToken()
                    platformLog(TAG, "TokenRefresh result: ${if (refreshed != null) "success" else "failure"}, and refresh : $refreshed")
                    if (refreshed == null) {
                        // Keep behavior close to legacy Retrofit authenticator: clear stale tokens and force re-auth.
                        AppStorage.clearAuthData()
                        AuthEventManager.requireReAuthentication()
                        platformLog(TAG, "TokenRefresh failed; cleared auth data and triggered re-authentication")
                        return@refreshTokens null
                    }
                    BearerTokens(refreshed.accessToken, refreshed.refreshToken)
                }
                sendWithoutRequest { request ->
                    // Return true only for routes that should include bearer preemptively.
                    val host = request.url.host.lowercase()
                    val path = request.url.pathSegments.joinToString("/").lowercase()

                    // Pre-signed S3 URLs carry AWS query-string signing; adding an Authorization
                    // header causes "Only one auth mechanism allowed" (400).
                    val isS3PreSigned = host.contains("amazonaws.com")

                    val isPublicEndpoint =
                        path.endsWith("auth/signin") ||
                        path.endsWith("auth/signout") ||
                        path.contains("daspay-configuration")

                    !isPublicEndpoint && !isS3PreSigned
                }
            }
        }
    }
}

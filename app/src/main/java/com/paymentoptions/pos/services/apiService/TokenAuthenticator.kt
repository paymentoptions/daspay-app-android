package com.paymentoptions.pos.services.apiService

import android.content.Context
import com.paymentoptions.pos.logger.AppLogger
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(private val context: Context) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite retry loops
        if (responseCount(response) >= 2) return null

        // Synchronously refresh the token
        AppLogger.debug("TokenAuthenticator refreshing token")
        val repository = TokenRepository.getInstance(context = context)
        val authDetails = repository.getAuthToken() ?: return null
        val newToken = kotlinx.coroutines.runBlocking { repository.doRefreshToken(authDetails) }

        // Generate new headers with the refreshed token
        val headers = generateRequestHeader(authToken = newToken?.data?.token?.idToken ?: "")

        // Build a new request with updated headers
        val builder = response.request.newBuilder()
        headers.forEach { (key, value) ->
            builder.header(key, value)
        }
        return builder.build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }
        return count
    }
}

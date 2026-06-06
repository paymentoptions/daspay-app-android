package com.paymentoptions.pos.utils

import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.ApiHttpException
import com.paymentoptions.pos.network.isNoNetworkError
import com.paymentoptions.pos.network.isTimeoutError
import org.json.JSONObject

/**
 * Parses the API error message from a Throwable.
 * It looks for JSON payloads in both ApiHttpException body and general exception messages.
 */
fun parseApiErrorMessage(error: Throwable, fallback: String): String {
    if (error.isTimeoutError()) {
        return "The request timed out. Please check your internet connection and try again."
    }

    if (error.isNoNetworkError()) {
        return "No internet connection. Please check your network settings and try again."
    }

    fun extractJsonPayload(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        if (start >= 0 && end > start) return raw.substring(start, end + 1)
        return null
    }

    fun parseGatewayMessage(jsonRaw: String?): String? {
        val payload = extractJsonPayload(jsonRaw) ?: return null
        return runCatching {
            val jsonObj = JSONObject(payload)
            jsonObj.optJSONObject("gateway_response")?.optString("message")
                ?.takeIf { it.isNotBlank() }
                ?: jsonObj.optString("message").takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    val parsedMessage = if (error is ApiHttpException) {
        parseGatewayMessage(error.responseBody) ?: parseGatewayMessage(error.message)
    } else {
        parseGatewayMessage(error.message)
    }

    val result = parsedMessage ?: error.message ?: fallback

    AppLogger.error(
        "parseApiErrorMessage",
        "Original Error: ${error.message}",
        "Parsed Result: $result",
        "Throwable: $error"
    )

    return result
}

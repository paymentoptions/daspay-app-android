package com.paymentoptions.pos.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.apache.http.conn.ConnectTimeoutException
import java.net.SocketTimeoutException

/**
 * Parses the API error message from a Throwable.
 * It looks for JSON payloads in both ApiHttpException body and general exception messages.
 */
fun parseApiErrorMessage(error: retrofit2.HttpException, fallback: String): String {
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
            val element = Json { ignoreUnknownKeys = true }.parseToJsonElement(payload).jsonObject
            element["gateway_response"]?.jsonObject?.get("message")?.jsonPrimitive?.content
                ?.takeIf { it.isNotBlank() }
                ?: element["message"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    val parsedMessage = parseGatewayMessage(error.response()?.errorBody()?.string()) ?: parseGatewayMessage(error.message)

    return parsedMessage ?: error.message ?: fallback
}

fun Throwable.isTimeoutError(): Boolean =
    this is ConnectTimeoutException ||
            this is SocketTimeoutException

/**
 * Checks if the [Throwable] is a network connection error (DNS, No Route, etc).
 */
fun Throwable.isNoNetworkError(): Boolean {
    val className = this::class.simpleName ?: ""
    return className.contains("UnknownHostException") ||
            className.contains("ConnectException") ||
            className.contains("NoRouteToHostException")
}

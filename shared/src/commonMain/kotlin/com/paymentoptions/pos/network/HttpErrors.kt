package com.paymentoptions.pos.network

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

/**
 * Retrofit HttpException equivalent for Ktor calls.
 */
class ApiHttpException(
    val statusCode: Int,
    val responseBody: String,
    endpointTag: String,
) : Exception("HTTP $statusCode at $endpointTag: $responseBody")

/**
 * Throws [ApiHttpException] on non-2xx responses.
 */
suspend fun HttpResponse.throwIfNotSuccess(endpointTag: String) {
    if (status.isSuccess()) return
    val errorText = runCatching { bodyAsText() }.getOrDefault("")
    throw ApiHttpException(status.value, errorText, endpointTag)
}

/**
 * Checks if the [Throwable] is a timeout error.
 */
fun Throwable.isTimeoutError(): Boolean =
    this is HttpRequestTimeoutException ||
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

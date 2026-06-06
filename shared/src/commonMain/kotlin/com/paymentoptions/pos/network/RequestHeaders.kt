package com.paymentoptions.pos.network

import com.paymentoptions.pos.storage.AppStorage
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header

private const val DASH_API_KEY = "bLm8c1C0fL3FtPzrjSr0"
private const val DEFAULT_X_AUTH = "F_FvwWj_L1wHkrMeg2c3Iv0Te52j_aJm"

/**
 * Applies common browser-like fingerprint headers used by most Daspay API requests.
 */
private fun HttpRequestBuilder.applyBrowserFingerprint() {
    header("accept-language",    "en-US,en;q=0.9")
    header("cache-control",      "no-cache")
    header("pragma",             "no-cache")
    header("priority",           "u=1, i")
    header("sec-ch-ua",          "\"Google Chrome\";v=\"135\", \"Not-A.Brand\";v=\"8\", \"Chromium\";v=\"135\"")
    header("sec-ch-ua-platform", "Windows")
    header("sec-ch-ua-mobile",   "?0")
    header("sec-fetch-dest",     "empty")
    header("sec-fetch-mode",     "cors")
    header("sec-fetch-site",     "cross-site")
    header("user-agent",         "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
    header("x-api-key",          DASH_API_KEY)
}

/**
 * Shared logic for applying base Daspay headers including auth and fingerprinting.
 */
private fun HttpRequestBuilder.applyBaseDaspayHeaders(
    origin: String = "http://dev.paymentoptions.com",
    referer: String = "http://dev.paymentoptions.com/",
    xAuthorization: String = DEFAULT_X_AUTH
) {
    applyBrowserFingerprint()
    header("origin", origin)
    header("referer", referer)
    header("x-authorization", xAuthorization)
    header("authorization", AppStorage.idToken ?: "")
}

/**
 * Applies all required Daspay API request headers to a Ktor request builder.
 */
fun HttpRequestBuilder.applyDaspayHeaders() {
    applyBaseDaspayHeaders()
}

fun HttpRequestBuilder.applyDaspaySignHeaders(isAndroid: Boolean, deviceNumber: String) {
    header("x-domain", "Daspay-Android-$deviceNumber")
    applyDaspayHeaders()
}

fun HttpRequestBuilder.applyRefundRequestHeader() {
    header("accept", "*/*")
    header("content-type", "application/json")
    header("Cookie", "reqid=5ddc7193-ac13-4517-a104-2ec6f2057215")
    applyBaseDaspayHeaders(
        origin = "https://api-dev.paymentoptions.com",
        referer = "https://api-dev.paymentoptions.com",
        xAuthorization = "-Qt4aQg9lb4I1rOTJGZdFXTXCh13UjcS"
    )
}

fun HttpRequestBuilder.applyPaymentRequestHeader() {
    header("x-secret-key", "We@ve")
    header("x-api-key", DASH_API_KEY)
    header("content-type", "application/json")
    header("Cookie", "reqid=9ba02754-19cb-4b47-ba3b-e656f04cd7d7")
    header("Authorization", AppStorage.idToken ?: "")
}

fun HttpRequestBuilder.applyPaymentStatusHeader() {
    header("content-type", "application/json")
    header("Cookie", "reqid=undefined; reqid=undefined; reqid=undefined")
}

fun HttpRequestBuilder. applySignatureUploadHeader() {
    header("accept", "*/*")
    applyBaseDaspayHeaders(
        origin = "https://api-dev.paymentoptions.com",
        referer = "https://api-dev.paymentoptions.com/"
    )
}

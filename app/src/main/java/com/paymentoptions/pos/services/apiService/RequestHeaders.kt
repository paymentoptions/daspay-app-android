package com.paymentoptions.pos.services.apiService

fun generateRequestHeaders(authToken: String = ""): Map<String, String> {
    val requestHeaders = mapOf<String, String>(
        "accept" to "*/*",
        "accept-language" to "en-GB,en;q=0.9",
        "cache-control" to "no-cache",
        "content-type" to "application/json",
        "origin" to "http://dev.paymentoptions.com",
        "pragma" to "no-cache",
        "priority" to "u=1, i",
        "referer" to "http://dev.paymentoptions.com/",
        "sec-ch-ua-mobile" to "?0",
        "sec-fetch-dest" to "empty",
        "sec-fetch-mode" to "cors",
        "sec-fetch-site" to "cross-site",
        "user-agent" to "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
        "x-api-key" to "bLm8c1C0fL3FtPzrjSr0",
        "authorization" to authToken
    )

    println("requestHeaders: $requestHeaders")
    return requestHeaders
}
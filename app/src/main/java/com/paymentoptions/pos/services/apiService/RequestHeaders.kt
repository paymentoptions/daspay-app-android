package com.paymentoptions.pos.services.apiService

fun generateRequestHeaders(authToken: String = ""): Map<String, String> {
    val headers = mapOf<String, String>(
        "accept" to "*/*",
        "accept-language" to "en-US,en;q=0.9",
        "cache-control" to "no-cache",
        "content-type" to "application/json",
        "origin" to "http://dev.paymentoptions.com",
        "pragma" to "no-cache",
        "priority" to "u=1, i",
        "referer" to "http://dev.paymentoptions.com/",
        "sec-ch-ua" to "\"Google Chrome\";v=\"135\", \"Not-A.Brand\";v=\"8\", \"Chromium\";v=\"135\"",
        "sec-ch-ua-platform" to "Windows",
        "sec-ch-ua-mobile" to "?0",
        "sec-fetch-dest" to "empty",
        "sec-fetch-mode" to "cors",
        "sec-fetch-site" to "cross-site",
        "user-agent" to "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
        "x-api-key" to "bLm8c1C0fL3FtPzrjSr0",
        "x-authorization" to "F_FvwWj_L1wHkrMeg2c3Iv0Te52j_aJm",

        //for refund only ----------------------------------------
//        "x-authorization" to "-Qt4aQg9lb4I1rOTJGZdFXTXCh13UjcS",
//        "Cookie" to "reqid=5ddc7193-ac13-4517-a104-2ec6f2057215",
        //---------------------------------------------------------

        "authorization" to authToken
    )

    println("requestHeaders: $headers")
    return headers
}

fun generatePaymentRequestHeaders(authToken: String = ""): Map<String, String> {
    val headers = mapOf<String, String>(
        "x-secret-key" to "We@ve",
        "x-api-key" to "bLm8c1C0fL3FtPzrjSr0",
        "content-type" to "application/json",
        "Cookie" to "reqid=9ba02754-19cb-4b47-ba3b-e656f04cd7d7",
        "Authorization" to authToken
    )

    println("generatePaymentRequestHeaders: $headers")
    return headers
}
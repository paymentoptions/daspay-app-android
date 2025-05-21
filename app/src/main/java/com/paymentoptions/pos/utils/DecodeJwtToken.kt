package com.paymentoptions.pos.utils

import android.util.Base64
import org.json.JSONObject

fun decodeJwtPayload(token: String): JSONObject {
    val parts = token.split(".")
    if (parts.size != 3) throw IllegalArgumentException("Invalid JWT token")

    val payloadBase64 = parts[1]
    val decodedBytes =
        Base64.decode(payloadBase64, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    val decodedPayload = String(decodedBytes, Charsets.UTF_8)
    return JSONObject(decodedPayload)
}

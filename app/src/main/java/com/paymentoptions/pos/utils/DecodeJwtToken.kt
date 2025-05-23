package com.paymentoptions.pos.utils

import android.util.Base64
import org.json.JSONArray
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

fun getDasmidFromToken(decodedTokenJson: JSONObject): String {
    val rawList = decodedTokenJson["custom:DASMID"] as? String
    val jsonArray = JSONArray(rawList)
    val result = mutableListOf<String>()
    for (i in 0 until jsonArray.length()) {
        result.add(jsonArray.getString(i))
    }
    val dasmid = result[result.size - 1]
    return dasmid
}

fun getKeyFromToken(decodedTokenJson: JSONObject, key: String): String {
    return decodedTokenJson[key] as String
}

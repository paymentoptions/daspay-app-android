package com.paymentoptions.pos.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun decodeJwtPayload(token: String): JsonObject {
    val parts = token.split(".")
    if (parts.size != 3) throw IllegalArgumentException("Invalid JWT token")

    val payloadBase64 = parts[1]
    val decodedPayload = decodeBase64(payloadBase64)
    return Json.parseToJsonElement(decodedPayload).jsonObject
}

fun getKeyFromToken(decodedTokenJson: JsonObject, key: String): String {
    return try {
        decodedTokenJson[key]?.jsonPrimitive?.content ?: ""
    } catch (_: Exception) {
        "No data from API"
    }
}

fun getDasmidFromToken(decodedTokenJson: JsonObject): String {
    val dasmidList = getKeyFromToken(decodedTokenJson = decodedTokenJson, key = "custom:DASMID")
    return try {
        val jsonArray = Json.parseToJsonElement(dasmidList).jsonArray
        jsonArray[0].jsonPrimitive.content
    } catch (_: Exception) {
        ""
    }
}

fun getMerchantIdFromToken(decodedTokenJson: JsonObject): String {
    return getKeyFromToken(decodedTokenJson = decodedTokenJson, key = "custom:MerchantID")
}

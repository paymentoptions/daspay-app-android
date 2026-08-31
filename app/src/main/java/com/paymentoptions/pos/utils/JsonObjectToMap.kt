package com.paymentoptions.pos.utils

import com.google.gson.Gson
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.AquirerResponse
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.json.JSONObject

fun jsonObjectToMap(jsonObject: JSONObject): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    val keys = jsonObject.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        var value = jsonObject.get(key)
        value = when (value) {
            is JSONObject -> jsonObjectToMap(value) // Recursively convert nested JSONObject
            else -> value
        }
        map[key] = value
    }
    return map
}


fun parseAcquirerResponse(raw: List<JsonElement>?): AquirerResponse? {
    val gson = Gson()
    return raw?.firstNotNullOfOrNull { element ->
        try {
            when (element) {
                is JsonObject -> {
                    gson.fromJson(element, AquirerResponse::class.java)
                }
                is JsonPrimitive -> {
                     gson.fromJson(element.asString, AquirerResponse::class.java)
                }
                else -> null
            }
        } catch (e: Exception) {
            AppLogger.error("parseAcquirerResponse Exception in parsing acquirerResponse: $e")
            null
        }
    }
}
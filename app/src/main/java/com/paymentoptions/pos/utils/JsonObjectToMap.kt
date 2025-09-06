package com.paymentoptions.pos.utils

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
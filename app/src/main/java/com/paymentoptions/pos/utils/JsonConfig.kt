package com.paymentoptions.pos.utils

import kotlinx.serialization.json.Json

/**
 * Configured Json instance for decoding API responses.
 * This instance ignores unknown keys to prevent crashes when the API returns new fields.
 */
val AppJson = Json {
    ignoreUnknownKeys = true
}


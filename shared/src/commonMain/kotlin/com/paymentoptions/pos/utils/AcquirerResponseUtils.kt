package com.paymentoptions.pos.utils

import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.AquirerResponse
import com.paymentoptions.pos.network.PaymentDetailsResponseData
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

// Lenient on purpose: acquirer payloads vary by gateway, and AquirerResponse gives every field a
// default, so unknown keys and explicit nulls must not fail the whole parse.
private val acquirerJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}

/**
 * Returns the first entry of the payment-details `AcquirerResponse` array that parses as an
 * [AquirerResponse]. The backend sends each entry either as a JSON object or as a JSON-encoded
 * string, so both are accepted. Returns null when nothing parses.
 */
fun parseAcquirerResponse(raw: List<JsonElement>?): AquirerResponse? =
    raw?.firstNotNullOfOrNull { element ->
        try {
            when (element) {
                is JsonObject -> acquirerJson.decodeFromJsonElement(AquirerResponse.serializer(), element)
                is JsonPrimitive ->
                    if (element.isString && element.content.isNotBlank()) {
                        acquirerJson.decodeFromString(AquirerResponse.serializer(), element.content)
                    } else {
                        null
                    }
                else -> null
            }
        } catch (e: Exception) {
            AppLogger.error("parseAcquirerResponse: could not parse acquirer response entry: ${e.message}")
            null
        }
    }

/**
 * The note to show for a transaction: the acquirer's `gatewayNotes` when present, otherwise the
 * payment's `Referenceremark`. Trimmed; empty when neither is set.
 */
fun getGatewayNotes(
    paymentDetails: PaymentDetailsResponseData?,
    acquirerResponse: AquirerResponse?,
): String {
    acquirerResponse?.gatewayNotes?.takeIf { it.isNotBlank() }?.let { return it.trim() }
    paymentDetails?.Referenceremark?.takeIf { it.isNotBlank() }?.let { return it.trim() }
    return ""
}

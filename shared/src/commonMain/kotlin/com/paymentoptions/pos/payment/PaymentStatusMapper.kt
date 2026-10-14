package com.paymentoptions.pos.payment

import com.paymentoptions.pos.network.PaymentStatusRequest
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

/**
 * Builds a [PaymentStatusRequest] from a MineSec native transaction result.
 * On iOS the full SDK [Transaction] type is not available in Kotlin, so fields
 * are extracted from the JSON payload when present and sensible defaults are used otherwise.
 */
object PaymentStatusMapper {

    private val json = Json { ignoreUnknownKeys = true }

    fun fromMineSecResult(
        posReference: String,
        result: MineSecTransactionResult,
        amount: String,
        currency: String,
        parentUUID: String? = null,
        childUUID: String? = null,
    ): PaymentStatusRequest {

        val acquirerJson =
            result.acquirerResponseJson?.takeIf { it.isNotBlank() } ?: "{}"

        val fields = runCatching {
            json.parseToJsonElement(acquirerJson).jsonObject
        }.getOrDefault(JsonObject(emptyMap()))

        val now = Clock.System.now()
            .toEpochMilliseconds()
            .toString()

        fun field(
            key: String,
            fallback: String = ""
        ): String {
            val element = fields[key] ?: return fallback
            return (element as? JsonPrimitive)
                ?.contentOrNull
                ?: fallback
        }

        fun objectField(
            key: String
        ): JsonObject? {
            return fields[key] as? JsonObject
        }

        val amountJson = objectField("amount")
            ?.toString()
            ?: buildJsonObject {
                put("currency", currency)
                put("value", amount)
            }.toString()

        return PaymentStatusRequest(
            tranId = result.posReference ?: posReference,
            cvmPerformed = field("cvmPerformed", "UNKNOWN"),
            tsi = field("tsi", "null"),
            mcc = field("mcc"),
            merchantName = field("merchantName"),
            tranStatus = field(
                "tranStatus",
                if (result.success) "APPROVED" else "DECLINED"
            ),
            tranType = result.tranType ?: field("tranType", "SALE"),
            atc = field("atc", "null"),
            createdAt = field("createdAt", now),
            updatedAt = field("updatedAt", now),
            trace = field("trace"),
            callbackUrl = field("callbackUrl", "null"),
            entryMode = field("entryMode", "CONTACTLESS"),

            // IMPORTANT: amount is an object
            amount = amountJson,

            batchNo = field("batchNo", "0"),
            appName = field("appName", "null"),
            linkedTranId = result.posReference ?: posReference,
            merchantAddr = field("merchantAddr"),
            rrn = field("rrn", "null"),
            tc = field("tc", "null"),
            tvr = field("tvr", "null"),
            accountMasked = field("accountMasked", "null"),
            sdkId = field("sdkId", "null"),
            paymentMethod = field("paymentMethod", "CARD"),
            hostMessageFormat = field("hostMessageFormat"),
            aid = field("aid", "null"),
            acquirerResponse = acquirerJson,
            parentUUID = parentUUID,
            childUUID = childUUID,
        )
    }
}
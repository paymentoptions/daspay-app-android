package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.InsightsResponse
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.applyDaspayHeaders
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

suspend fun insights(
    deviceNumber: String,
    uniqueCode: String,
    dasmid: String? = null,
    startDate: String? = null,
    endDate: String? = null,
    timeZone: String? = null,
    amount: String? = null,
    id: String? = null,
    transactionType: String? = null,
    productType: String? = null,
    take: Int = 50,
    skip: Int = 0,
): InsightsResponse? {
    val response = KtorClient.instance.get(ConfigurationManager.url(ApiEndpoints.INSIGHTS)) {
        applyDaspayHeaders()
        parameter("deviceNumber", deviceNumber)
        parameter("uniqueCode", uniqueCode)
        dasmid?.let { parameter("dasmid", it) }
        startDate?.let { parameter("startDate", it) }
        endDate?.let { parameter("endDate", it) }
        timeZone?.let { parameter("timeZone", it) }
        amount?.let { parameter("amount", it) }
        id?.let { parameter("ID", it) }
        transactionType?.let { parameter("transactionType", it) }
        productType?.let { parameter("productType", it) }
        parameter("take", take)
        parameter("skip", skip)
    }
    response.throwIfNotSuccess("insights")
    return response.body<InsightsResponse>()
}

package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.SettlementListResponse
import com.paymentoptions.pos.network.applyPaymentRequestHeader
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

suspend fun settlementList(deviceNumber: String, uniqueCode: String): SettlementListResponse? {
    val response = KtorClient.instance.get(ConfigurationManager.url(ApiEndpoints.SETTLEMENT_LIST)) {
        applyPaymentRequestHeader()
        parameter("deviceNumber", deviceNumber)
        parameter("uniqueCode", uniqueCode)
    }
    response.throwIfNotSuccess("settlementList")
    return response.body<SettlementListResponse>()
}

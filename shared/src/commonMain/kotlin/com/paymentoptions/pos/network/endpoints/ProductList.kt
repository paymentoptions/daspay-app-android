package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.ProductListResponse
import com.paymentoptions.pos.network.applyDaspayHeaders
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.get

suspend fun productList(categoryId: String): ProductListResponse? {
    val url = ConfigurationManager.url(ApiEndpoints.PRODUCT_LIST, "categoryId" to categoryId)
    val response = KtorClient.instance.get(url) {
        applyDaspayHeaders()
    }
    response.throwIfNotSuccess("productList")
    return response.body<ProductListResponse>()
}

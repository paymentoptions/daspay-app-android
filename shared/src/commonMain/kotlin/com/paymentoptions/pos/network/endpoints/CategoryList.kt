package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.CategoryListResponse
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.applyDaspayHeaders
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.get

suspend fun categoryList(merchantId: String): CategoryListResponse? {
    val url = ConfigurationManager.url(ApiEndpoints.CATEGORY_LIST, "merchantId" to merchantId)
    val response = KtorClient.instance.get(url) {
        applyDaspayHeaders()
    }
    response.throwIfNotSuccess("categoryList")
    return response.body<CategoryListResponse>()
}

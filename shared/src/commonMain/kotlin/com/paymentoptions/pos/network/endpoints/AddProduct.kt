package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.ProductImageRequest
import com.paymentoptions.pos.network.ProductRequest
import com.paymentoptions.pos.network.ProductResponse
import com.paymentoptions.pos.network.applyDaspayHeaders
import com.paymentoptions.pos.network.throwIfNotSuccess
import com.paymentoptions.pos.platformLog
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

private const val TAG = "AddProduct"

suspend fun addProduct(
    request: ProductRequest,
    imageBytes: ByteArray? = null,
    imageFileName: String? = null
): ProductResponse? {
    platformLog(TAG, "addProduct: $request, image provided: ${imageBytes != null}")
    
    val responseHttp = KtorClient.instance.post(ConfigurationManager.url(ApiEndpoints.ADD_PRODUCT)) {
        contentType(ContentType.Application.Json)
        applyDaspayHeaders()
        setBody(request)
    }
    responseHttp.throwIfNotSuccess("addProduct")
    val response = responseHttp.body<ProductResponse>()

    if (imageBytes != null && (response.statusCode == 200L || response.statusCode == 201L)) {
        val productId = response.data.ProductID
        val uploadResponse = uploadMedia(
            ProductImageRequest(
                ProductID = productId,
                fileName = imageFileName ?: "product_$productId.jpg"
            )
        )
        val signedUrl = uploadResponse?.data?.signedUrl
        if (!signedUrl.isNullOrBlank()) {
            try {
                val compressed = resizeAndCompressImage(imageBytes)
                uploadToS3(compressed, signedUrl)
                platformLog(TAG, "Image uploaded successfully for product: $productId")
            } catch (e: Exception) {
                platformLog(TAG, "Failed to upload image: ${e.message}")
            }
        }
    }
    
    return response
}

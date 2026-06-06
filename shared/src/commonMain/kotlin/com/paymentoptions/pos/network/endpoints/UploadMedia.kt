package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.ProductImageRequest
import com.paymentoptions.pos.network.UploadImageResponse
import com.paymentoptions.pos.network.applyDaspayHeaders
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

suspend fun uploadMedia(request: ProductImageRequest): UploadImageResponse? {
    val response = KtorClient.instance.post(ConfigurationManager.url(ApiEndpoints.UPLOAD_PRODUCT_IMAGE)) {
        contentType(ContentType.Application.Json)
        applyDaspayHeaders()
        setBody(request)
    }
    response.throwIfNotSuccess("uploadMedia")
    return response.body<UploadImageResponse>()
}

/**
 * Uploads raw image bytes to a pre-signed S3 URL.
 *
 * Pre-signed URLs are self-contained: they carry AWS query-string signing
 * (X-Amz-Algorithm, X-Amz-Signature, etc.). Adding an Authorization header
 * causes S3 to reject the request with 400 "Only one auth mechanism allowed".
 * — do NOT call applyDaspayHeaders() here.
 */
suspend fun uploadToS3(imageBytes: ByteArray, signedUrl: String) {
    val response = KtorClient.instance.put(signedUrl) {
        contentType(ContentType.Image.JPEG)
        // No applyDaspayHeaders — S3 pre-signed URL handles its own auth
        setBody(imageBytes)
    }
    if (!response.status.isSuccess()) {
        throw Exception("S3 upload failed: ${response.status.value} ${response.status.description}")
    }
}

/**
 * Platform-specific image resizing and compression.
 * Takes raw [imageBytes], returns compressed JPEG [ByteArray].
 */
expect suspend fun resizeAndCompressImage(
    imageBytes: ByteArray,
    maxSize: Int = 1280,
    quality: Int = 80
): ByteArray

package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.UploadSignatureResponse
import com.paymentoptions.pos.network.applySignatureUploadHeader
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

suspend fun uploadSignature(signatureBytes: ByteArray, transactionId: String): UploadSignatureResponse? {
    val response = KtorClient.instance.post(ConfigurationManager.url(ApiEndpoints.UPLOAD_SIGNATURE)) {
        applySignatureUploadHeader()
        parameter("TransactionID", transactionId)
        setBody(MultiPartFormDataContent(formData {
            append("signature", signatureBytes, Headers.build {
                append(HttpHeaders.ContentType, "image/png")
                append(HttpHeaders.ContentDisposition, "filename=\"signature.png\"")
            })
        }))
    }
    response.throwIfNotSuccess("uploadSignature")
    return response.body<UploadSignatureResponse>()
}

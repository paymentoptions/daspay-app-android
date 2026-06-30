package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.UploadSignatureResponse
import com.paymentoptions.pos.network.applySignatureUploadHeader
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.http.parameters
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class)
actual suspend fun uploadSignature(
    signatureBytes: ByteArray,
    transactionId: String,
): UploadSignatureResponse? {
    val nsData = signatureBytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = signatureBytes.size.toULong())
    }
    val base64String = nsData.base64EncodedStringWithOptions(0u)
    val signatureData = "data:image/png;base64,$base64String"

    AppLogger.debug(
        "uploadSignature",
        "Sending form-urlencoded — transactionId=$transactionId signatureLength=${signatureData.length}",
    )

    val response = KtorClient.instance.submitForm(
        url = ConfigurationManager.url(ApiEndpoints.UPLOAD_SIGNATURE),
        formParameters = parameters {
            append("signature", signatureData)
            append("TransactionID", transactionId)
        },
    ) {
        applySignatureUploadHeader()
    }

    response.throwIfNotSuccess("uploadSignature")
    return response.body<UploadSignatureResponse>()
}

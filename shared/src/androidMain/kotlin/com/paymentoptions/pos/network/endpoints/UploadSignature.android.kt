package com.paymentoptions.pos.network.endpoints

import android.os.Build
import androidx.annotation.RequiresApi
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.UploadSignatureResponse
import com.paymentoptions.pos.network.applySignatureUploadHeader
import com.paymentoptions.pos.network.throwIfNotSuccess
import io.ktor.client.call.body

import java.util.Base64  // ✅ Stable, no line breaks, available on Android API 26+, have to find Base 64 equivalent for IOS


import io.ktor.client.request.forms.submitForm
import io.ktor.http.parameters

@RequiresApi(Build.VERSION_CODES.O)
actual suspend fun uploadSignature(signatureBytes: ByteArray, transactionId: String): UploadSignatureResponse? {

    // ✅ FIX: Old Retrofit used @FormUrlEncoded + @Field — that is application/x-www-form-urlencoded.
    // Ktor's MultiPartFormDataContent sends multipart/form-data which this API rejects (ERR_MS_0043).
    // submitForm() is the Ktor equivalent of @FormUrlEncoded — it sets the correct Content-Type.
    val base64String = Base64.getEncoder().encodeToString(signatureBytes)
    val signatureData = "data:image/png;base64,$base64String"

    AppLogger.debug("uploadSignature", "Sending form-urlencoded — transactionId=$transactionId signatureLength=${signatureData.length}")

    val response = KtorClient.instance.submitForm(
        url = ConfigurationManager.url(ApiEndpoints.UPLOAD_SIGNATURE),
        formParameters = parameters {
            append("signature", signatureData)
            append("TransactionID", transactionId)
        }
    ) {
        applySignatureUploadHeader()
    }

    response.throwIfNotSuccess("uploadSignature")
    return response.body<UploadSignatureResponse>()
}
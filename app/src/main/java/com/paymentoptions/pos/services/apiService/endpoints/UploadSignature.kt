package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.UploadSignatureResponse
import com.paymentoptions.pos.services.apiService.generateSignatureUploadHeader
import com.paymentoptions.pos.services.apiService.shouldRefreshToken
import java.io.ByteArrayOutputStream

//Converts a Bitmap to a Base64 encoded string.
private fun bitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    //Compress the bitmap as a PNG
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    //Encode the byte array to Base64
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}

//Uploads the signature bitmap for a transaction
suspend fun uploadSignature(
    context: Context,
    signatureBitmap: Bitmap,
    transactionId: String
): UploadSignatureResponse? {
    try {
        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded() ?: return null

        val idToken = authDetails.data.token.idToken
        val requestHeaders = generateSignatureUploadHeader(idToken)

        // Convert Bitmap to Base64 String and add the required data URI prefix
        val signatureBase64 = bitmapToBase64(signatureBitmap)
        val signatureData = "data:image/png;base64,$signatureBase64"

        AppLogger.debug("Uploading Signature for TransactionID: $transactionId")
        val response = RetrofitClient.getApi(context).uploadSignature(
            headers = requestHeaders,
            signature = signatureData,
            TransactionID = transactionId
        )

        AppLogger.debug("UploadSignature Response: $response")
        return response

    } catch (e: Exception) {
        AppLogger.error("UploadSignatureError: ${e.message}")
        throw e
    }
}
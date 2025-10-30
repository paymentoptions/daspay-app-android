package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.RetrofitClient
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
        var authDetails = SharedPreferences.getAuthDetails(context)
        val username = authDetails?.data?.email ?: ""
        val refreshToken = authDetails?.data?.token?.refreshToken ?: ""
        val shouldRefreshToken = shouldRefreshToken(authDetails?.data?.exp)

        if (shouldRefreshToken) {
            authDetails = refreshTokens(context, username, refreshToken)
        }

        val idToken = authDetails?.data?.token?.idToken
        val requestHeaders = generateSignatureUploadHeader(idToken ?: "")

        // Convert Bitmap to Base64 String and add the required data URI prefix
        val signatureBase64 = bitmapToBase64(signatureBitmap)
        val signatureData = "data:image/png;base64,$signatureBase64"

        println("Uploading Signature for TransactionID: $transactionId")
        val response = RetrofitClient.api.uploadSignature(
            headers = requestHeaders,
            signature = signatureData,
            TransactionID = transactionId
        )

        println("UploadSignature Response: $response")
        return response

    } catch (e: Exception) {
        println("UploadSignatureError: ${e.message}")
        throw e
    }
}
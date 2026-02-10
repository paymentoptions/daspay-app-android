package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import com.paymentoptions.pos.services.apiService.ProductImageRequest
import com.paymentoptions.pos.services.apiService.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * Upload Media to product id.
 * Compress the image before sending to server
 */
suspend fun uploadMediaToProduct(
    context: Context,
    requestHeaders: Map<String, String>,
    productId: String,
    selectedFile: File
) {
    val uploadImageResponse =
        RetrofitClient.getApi(context).uploadProductImage(
            headers = requestHeaders,
            request = ProductImageRequest(
                ProductID = productId,
                fileName = selectedFile.absolutePath
            )
        )
    val signedUrl = uploadImageResponse.data.signedUrl
    if (signedUrl.isNotBlank()) {

        val compressedFile = resizeAndCompressFile(
            inputFile = selectedFile,
            outputDir = context.cacheDir
        )

        uploadToS3(
            compressedFile = compressedFile,
            signedUrl = signedUrl
        )
    } else {
        println("No signed URL returned for S3 upload")
    }
}

fun uploadToS3(
    compressedFile: File,
    signedUrl: String
) {
    val client = OkHttpClient()

    val mediaType = "image/jpeg".toMediaTypeOrNull()
    val requestBody = compressedFile.asRequestBody(mediaType)

    val request = Request.Builder()
        .url(signedUrl)
        .put(requestBody)
        .addHeader("Content-Type", "image/jpeg")
        .addHeader("Content-Length", compressedFile.length().toString())
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw Exception("S3 upload failed: ${response.code} ${response.message}")
        }
    }
}


fun resizeAndCompressFile(
    inputFile: File,
    outputDir: File,
    maxSize: Int = 1280,
    quality: Int = 80
): File {

    // 🔹 Step 1: Read image bounds only (no bitmap yet)
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeFile(inputFile.absolutePath, options)

    // 🔹 Step 2: Calculate sample size
    options.inSampleSize = calculateInSampleSize(options, maxSize, maxSize)
    options.inJustDecodeBounds = false

    // 🔹 Step 3: Decode scaled bitmap (memory-safe)
    val bitmap = BitmapFactory.decodeFile(inputFile.absolutePath, options)
        ?: throw IllegalStateException("Failed to decode image")

    // 🔹 Step 4: Resize to exact maxSize (optional but recommended)
    val resizedBitmap = resizeBitmap(bitmap, maxSize)

    // 🔹 Step 5: Compress
    val outFile = File(
        outputDir,
        "compressed_${System.currentTimeMillis()}.jpg"
    )

    FileOutputStream(outFile).use { out ->
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
    }

    bitmap.recycle()
    resizedBitmap.recycle()

    println("resizeAndCompressFile outFile: $outFile")

    return outFile
}


fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val (height: Int, width: Int) = options.outHeight to options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        var halfHeight = height / 2
        var halfWidth = width / 2

        while (
            (halfHeight / inSampleSize) >= reqHeight &&
            (halfWidth / inSampleSize) >= reqWidth
        ) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}


fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
    val ratio = bitmap.width.toFloat() / bitmap.height
    val width: Int
    val height: Int

    if (ratio > 1) {
        width = maxSize
        height = (maxSize / ratio).toInt()
    } else {
        height = maxSize
        width = (maxSize * ratio).toInt()
    }

    return bitmap.scale(width, height)
}



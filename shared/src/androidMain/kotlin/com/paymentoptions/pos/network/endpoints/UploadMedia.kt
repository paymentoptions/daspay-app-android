@file:JvmName("UploadMediaAndroid")
package com.paymentoptions.pos.network.endpoints

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import com.paymentoptions.pos.platformLog
import java.io.ByteArrayOutputStream

actual suspend fun resizeAndCompressImage(
    imageBytes: ByteArray,
    maxSize: Int,
    quality: Int
): ByteArray {
    platformLog("UploadMedia", "Resizing and compressing image on Android. Original size: ${imageBytes.size}")
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)

    options.inSampleSize = calculateInSampleSize(options, maxSize, maxSize)
    options.inJustDecodeBounds = false

    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
        ?: throw IllegalStateException("Failed to decode image")

    val resizedBitmap = resizeBitmap(bitmap, maxSize)
    val stream = ByteArrayOutputStream()
    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

    val result = stream.toByteArray()
    bitmap.recycle()
    resizedBitmap.recycle()
    return result
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.outHeight to options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
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

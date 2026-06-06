package com.paymentoptions.pos.network.endpoints

import kotlinx.cinterop.*
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.*
import platform.UIKit.*
import platform.posix.memcpy
import com.paymentoptions.pos.platformLog

@OptIn(ExperimentalForeignApi::class)
actual suspend fun resizeAndCompressImage(
    imageBytes: ByteArray,
    maxSize: Int,
    quality: Int
): ByteArray {
    platformLog("UploadMedia", "Resizing and compressing image on iOS. Original size: ${imageBytes.size}")
    val data = imageBytes.toNSData()
    val image = UIImage.imageWithData(data) ?: throw IllegalStateException("Failed to decode image")

    val resizedImage = resizeUIImage(image, maxSize.toDouble())
    val compressedData = UIImageJPEGRepresentation(resizedImage, quality / 100.0)
        ?: throw IllegalStateException("Failed to compress image")

    return compressedData.toByteArray()
}

@OptIn(ExperimentalForeignApi::class)
private fun resizeUIImage(image: UIImage, maxSize: Double): UIImage {
    val size = image.size.useContents {
        val ratio = width / height
        if (ratio > 1) {
            CGSizeMake(maxSize, maxSize / ratio)
        } else {
            CGSizeMake(maxSize * ratio, maxSize)
        }
    }

    UIGraphicsBeginImageContextWithOptions(size, false, 1.0)
    image.drawInRect(CGRectMake(0.0, 0.0, size.useContents { width }, size.useContents { height }))
    val newImage = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()

    return newImage ?: image
}

// Helper extensions for NSData <-> ByteArray
@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val lengthInt = length.toInt()
    val byteArray = ByteArray(lengthInt)
    if (lengthInt > 0) {
        byteArray.usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, length)
        }
    }
    return byteArray
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()
    return usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), length = size.toULong())
    }
}

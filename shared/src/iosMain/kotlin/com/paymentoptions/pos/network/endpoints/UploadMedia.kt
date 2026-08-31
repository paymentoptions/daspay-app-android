@file:OptIn(ExperimentalForeignApi::class)
package com.paymentoptions.pos.network.endpoints

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy
import com.paymentoptions.pos.platformLog
import platform.Foundation.dataWithBytes


/**
 * ┌────────────────────────┬─────────────────────────────────────────┬──────────────────────────────────────────────┐
 * │          File          │                   Old                   │                     New                      │
 * ├────────────────────────┼─────────────────────────────────────────┼──────────────────────────────────────────────┤
 * │ UploadMedia.kt         │ image.size.useContents { width/height } │ CGImageGetWidth(image.CGImage) /             │
 * │                        │                                         │ CGImageGetHeight(image.CGImage)              │
 * ├────────────────────────┼─────────────────────────────────────────┼──────────────────────────────────────────────┤
 * │ ScreenRatioToDp.ios.kt │ UIScreen.mainScreen.bounds.useContents  │ CGRectGetHeight(UIScreen.mainScreen.bounds)  │
 * │                        │ { size.height }                         │                                              │
 * ├────────────────────────┼─────────────────────────────────────────┼──────────────────────────────────────────────┤
 * │ QRCodeGenerator.kt     │ outputImage.extent.useContents {        │ CGRectGetWidth(outputImage.extent)           │
 * │                        │ this.size.width }                       │                                              │
 * └────────────────────────┴─────────────────────────────────────────┴──────────────────────────────────────────────┘
 *
 * CGRectGetWidth, CGRectGetHeight, CGImageGetWidth, CGImageGetHeight are C functions in platform.CoreGraphics that accept
 * CValue<T> directly — they were specifically designed by Apple as safe, orientation-aware accessors for struct fields,
 * and they work fine in Kotlin/Native 2.x without useContents.
 */
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

private fun resizeUIImage(image: UIImage, maxSize: Double): UIImage {
    val cgImage = image.CGImage ?: return image
    val originalWidth = CGImageGetWidth(cgImage).toDouble()
    val originalHeight = CGImageGetHeight(cgImage).toDouble()

    val ratio = originalWidth / originalHeight
    val newWidth: Double
    val newHeight: Double
    if (ratio > 1) {
        newWidth = maxSize
        newHeight = maxSize / ratio
    } else {
        newWidth = maxSize * ratio
        newHeight = maxSize
    }

    UIGraphicsBeginImageContextWithOptions(CGSizeMake(newWidth, newHeight), false, 1.0)
    image.drawInRect(CGRectMake(0.0, 0.0, newWidth, newHeight))
    val newImage = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()

    return newImage ?: image
}

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

private fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()
    return usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), length = size.toULong())
    }
}

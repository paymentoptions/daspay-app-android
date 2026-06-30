package com.paymentoptions.pos.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ImageInfo
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGRectMake
import platform.CoreImage.CIContext
import platform.CoreImage.CIFilter
import platform.CoreImage.filterWithName
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun generateQrCode(text: String, size: Int): ImageBitmap? {
    return try {
        val messageData = (text as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return null

        // Pass inputs via filterWithName:withInputParameters: — avoids KVC setValue:forKey:
        // which is shadowed by Kotlin property-delegate setValue extensions in K2.
        // K2 bridges Kotlin Map<Any?,*>? to NSDictionary; "inputMessage" = kCIInputMessageKey value.
        val filter = CIFilter.filterWithName(
            "CIQRCodeGenerator",
            withInputParameters = mapOf<Any?, Any>("inputMessage" to messageData)
        ) ?: return null

        val outputImage = filter.outputImage ?: return null
        val extentWidth = outputImage.extent.useContents { this.size.width }
        val scaleFactor = size.toDouble() / extentWidth.coerceAtLeast(1.0)
        val scaledImage = outputImage.imageByApplyingTransform(
            CGAffineTransformMakeScale(scaleFactor, scaleFactor)
        ) ?: return null

        // Render CIImage into a CGBitmapContext directly.
        // CIContext.createCGImage is not exposed by K2 Kotlin/Native with Xcode 26,
        // and UIImage(CIImage:) init is also inaccessible. Using contextWithCGContext
        // + drawImage:inRect:fromRect: writes pixels straight into our byte buffer.
        val colorSpace = CGColorSpaceCreateDeviceRGB()
        val bytesPerRow = size * 4
        val pixelData = ByteArray(bytesPerRow * size)

        var rendered = false
        pixelData.usePinned { pinned ->
            val bitmapCtx = CGBitmapContextCreate(
                data = pinned.addressOf(0),
                width = size.toULong(),
                height = size.toULong(),
                bitsPerComponent = 8u,
                bytesPerRow = bytesPerRow.toULong(),
                space = colorSpace,
                bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value,
            ) ?: return@usePinned

            val ciCtx = CIContext.contextWithCGContext(bitmapCtx, options = null)
            val bounds = CGRectMake(0.0, 0.0, size.toDouble(), size.toDouble())
            ciCtx.drawImage(scaledImage, inRect = bounds, fromRect = scaledImage.extent)
            rendered = true
        }

        if (!rendered) return null

        val skiaBitmap = Bitmap()
        skiaBitmap.allocPixels(ImageInfo.makeN32(size, size, ColorAlphaType.PREMUL))
        skiaBitmap.installPixels(skiaBitmap.imageInfo, pixelData, bytesPerRow)
        skiaBitmap.asComposeImageBitmap()
    } catch (_: Exception) {
        null
    }
}

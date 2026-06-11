package com.paymentoptions.pos.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import platform.CoreGraphics.CGRectMake
import platform.CoreImage.CIFilter
import platform.CoreImage.CIImage
import platform.CoreImage.filterWithName
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIColor
import platform.CoreImage.kCIInputMessageKey

actual fun generateQrCode(text: String, size: Int): ImageBitmap? {
    val filter = CIFilter.filterWithName("CIQRCodeGenerator") ?: return null
    filter.setDefaults()
    
    val data = (text as NSString).dataUsingEncoding(NSUTF8StringEncoding)
    filter.setValue(data, forKey = kCIInputMessageKey)
    
    val outputImage = filter.outputImage ?: return null
    
    // Scale the image to the requested size
    val scaleX = size.toDouble() / outputImage.extent.size.width
    val scaleY = size.toDouble() / outputImage.extent.size.height
    val transformedImage = outputImage.imageByApplyingTransform(
        platform.CoreGraphics.CGAffineTransformMakeScale(scaleX, scaleY)
    )
    
    val uiImage = UIImage(CIImage = transformedImage)
    return uiImage.toComposeImageBitmap()
}

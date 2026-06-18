package com.paymentoptions.pos.utils

import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.NSDataBase64DecodingIgnoreUnknownCharacters
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding

actual fun decodeBase64(input: String): String {
    // Add padding if missing
    val padded = when (input.length % 4) {
        2 -> "$input=="
        3 -> "$input="
        else -> input
    }.replace('-', '+').replace('_', '/')

    val data = NSData.create(base64EncodedString = padded, options = NSDataBase64DecodingIgnoreUnknownCharacters)
        ?: return ""
    
    return NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String ?: ""
}

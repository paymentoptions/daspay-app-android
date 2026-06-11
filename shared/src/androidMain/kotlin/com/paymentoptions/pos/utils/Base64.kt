package com.paymentoptions.pos.utils

import android.util.Base64

actual fun decodeBase64(input: String): String {
    val decodedBytes = Base64.decode(input, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    return String(decodedBytes, Charsets.UTF_8)
}

package com.paymentoptions.pos.utils

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Generates a QR code from the given text.
 * Returns an ImageBitmap that can be displayed in a Compose Image.
 */
expect fun generateQrCode(text: String, size: Int = 512): ImageBitmap?

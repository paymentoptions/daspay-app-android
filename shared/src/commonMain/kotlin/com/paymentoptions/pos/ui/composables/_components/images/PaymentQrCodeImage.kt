package com.paymentoptions.pos.ui.composables._components.images

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale

@Composable
fun PaymentQrCodeImage(modifier: Modifier = Modifier, qrBitmap: ImageBitmap? = null) {
    qrBitmap?.let {
        Image(
            bitmap = qrBitmap,
            contentDescription = "Generated QR code for payment",
            contentScale = ContentScale.Fit,
            modifier = modifier
        )
    }
}

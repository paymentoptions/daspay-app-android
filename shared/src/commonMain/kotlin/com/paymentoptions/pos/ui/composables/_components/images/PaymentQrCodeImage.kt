package com.paymentoptions.pos.ui.composables._components.images

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.clickable

@Composable
fun PaymentQrCodeImage(
    modifier: Modifier = Modifier,
    qrBitmap: ImageBitmap? = null,
    onClick: (() -> Unit)? = null,
) {
    qrBitmap?.let {
        Image(
            bitmap = qrBitmap,
            contentDescription = "Generated QR code for payment",
            contentScale = ContentScale.Fit,
            modifier = if (onClick != null) modifier.clickable { onClick() } else modifier
        )
    }
}

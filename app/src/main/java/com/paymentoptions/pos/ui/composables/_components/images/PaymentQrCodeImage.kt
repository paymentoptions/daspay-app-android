package com.paymentoptions.pos.ui.composables._components.images

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

@Composable
fun PaymentQrCodeImage(
    modifier: Modifier = Modifier,
    qrBitmap: Bitmap? = null,
    onClick: (() -> Unit)? = null
) {
    qrBitmap?.let {
        val imageModifier = if (onClick != null) {
            modifier.clickable { onClick() }
        } else {
            modifier
        }
        Image(
            bitmap = qrBitmap.asImageBitmap(),
            contentDescription = "Generated QR code for payment",
            contentScale = ContentScale.Fit,
            modifier = imageModifier
        )
    }
}

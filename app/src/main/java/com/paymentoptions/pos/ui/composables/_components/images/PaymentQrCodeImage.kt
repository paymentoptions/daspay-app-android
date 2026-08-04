package com.paymentoptions.pos.ui.composables._components.images

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

@Composable
fun PaymentQrCodeImage(modifier: Modifier = Modifier, qrBitmap: Bitmap? = null) {
    qrBitmap?.let {
        // if a bitmap is provided display it
        Image(
            bitmap = qrBitmap.asImageBitmap(),
            contentDescription = "Generated QR code for payment",
            contentScale = ContentScale.Fit,
            modifier = modifier
        )
    }
}
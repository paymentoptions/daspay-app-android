package com.paymentoptions.pos.ui.composables._components.images.qrpayment

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.paymentoptions.pos.R

@Composable
fun AliPayImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ali_pay),
        contentDescription = "Qr code payment method",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}
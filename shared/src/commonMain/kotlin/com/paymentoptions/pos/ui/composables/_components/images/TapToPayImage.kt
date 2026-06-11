package com.paymentoptions.pos.ui.composables._components.images

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.tap_to_pay

@Composable
fun TapToPayImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.tap_to_pay),
        contentDescription = "Tap to pay",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

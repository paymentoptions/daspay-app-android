package com.paymentoptions.pos.ui.composables._components.images.apms

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.applepay_apms

@Composable
fun ApplePayImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.applepay_apms),
        contentDescription = "Apple Pay",
        modifier = modifier
    )
}

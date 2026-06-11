package com.paymentoptions.pos.ui.composables._components.images

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.payment_taptopay

@Composable
fun PaymentTapToPayImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.payment_taptopay),
        contentDescription = "Payment Tap to Pay",
        modifier = modifier
    )
}

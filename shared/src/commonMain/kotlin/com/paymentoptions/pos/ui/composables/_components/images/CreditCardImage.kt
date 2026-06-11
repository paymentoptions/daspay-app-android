package com.paymentoptions.pos.ui.composables._components.images

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.credit_card

@Composable
fun CreditCardImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.credit_card),
        contentDescription = "Credit Card",
        modifier = modifier
    )
}

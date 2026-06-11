package com.paymentoptions.pos.ui.composables._components.images.schemes

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.mastercard_scheme

@Composable
fun MastercardImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.mastercard_scheme),
        contentDescription = "Mastercard",
        modifier = modifier
    )
}

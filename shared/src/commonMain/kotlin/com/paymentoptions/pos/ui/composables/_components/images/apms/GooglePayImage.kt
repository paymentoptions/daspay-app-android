package com.paymentoptions.pos.ui.composables._components.images.apms

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.googlepay_apms

@Composable
fun GooglePayImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.googlepay_apms),
        contentDescription = "Google Pay",
        modifier = modifier
    )
}

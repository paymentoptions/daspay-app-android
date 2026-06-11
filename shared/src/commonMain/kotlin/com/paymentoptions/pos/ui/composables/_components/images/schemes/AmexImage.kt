package com.paymentoptions.pos.ui.composables._components.images.schemes

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.amex_scheme

@Composable
fun AmexImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.amex_scheme),
        contentDescription = "Amex",
        modifier = modifier
    )
}

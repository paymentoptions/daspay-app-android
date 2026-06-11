package com.paymentoptions.pos.ui.composables._components.images

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.pay_by_link

@Composable
fun PayByLinkImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.pay_by_link),
        contentDescription = "Pay By Link",
        modifier = modifier
    )
}

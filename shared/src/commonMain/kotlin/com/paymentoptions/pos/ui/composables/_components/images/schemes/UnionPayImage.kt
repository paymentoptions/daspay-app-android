package com.paymentoptions.pos.ui.composables._components.images.schemes

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.union_pay

@Composable
fun UnionPayImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.union_pay),
        contentDescription = "UnionPay",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

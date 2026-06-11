package com.paymentoptions.pos.ui.composables._components.images

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.logo

@Composable
fun LogoImage(modifier: Modifier = Modifier, blurTopSection: Boolean = false) {
    Image(
        painter = painterResource(Res.drawable.logo),
        contentDescription = "DASPay Logo",
        modifier = modifier
            .blur(if (blurTopSection) 8.dp else 0.dp),
    )
}

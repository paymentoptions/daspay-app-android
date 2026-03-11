package com.paymentoptions.pos.ui.composables._components.images

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.paymentoptions.pos.R

@Composable
fun LogoImage(modifier: Modifier = Modifier,blurTopSection: Boolean = false) {
    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "DASPay Logo",
        modifier = modifier
            .blur(if (blurTopSection) 8.dp else 0.dp),
    )
}
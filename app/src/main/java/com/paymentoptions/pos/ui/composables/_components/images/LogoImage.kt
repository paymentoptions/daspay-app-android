package com.paymentoptions.pos.ui.composables._components.images

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paymentoptions.pos.R

@Composable
fun LogoImage(height: Dp = 60.dp) {
    Image(
        painter = painterResource(id = R.drawable.logo), // Replace with your image resource
        contentDescription = "DASPay Logo",
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    )
}
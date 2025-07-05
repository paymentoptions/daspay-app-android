package com.paymentoptions.pos.ui.composables._components.images

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paymentoptions.pos.R

@Composable
fun TapToPayImage(height: Dp = 260.dp) {
    Image(
        painter = painterResource(id = R.drawable.tap_to_pay), // Replace with your image resource
        contentDescription = "Tap to pay",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    )
}
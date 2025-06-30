package com.paymentoptions.pos.ui.composables._components.images

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.paymentoptions.pos.R

@Composable
fun BackgroundImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.banner_bg),
        contentDescription = "App Background",
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize()
    )
}
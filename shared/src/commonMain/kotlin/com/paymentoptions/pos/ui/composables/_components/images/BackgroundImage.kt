package com.paymentoptions.pos.ui.composables._components.images

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.banner_bg

@Composable
fun BackgroundImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.banner_bg),
        contentDescription = "App Background",
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

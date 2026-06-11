package com.paymentoptions.pos.ui.composables._components.images.loaders

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.loader_white

// TODO(KMP): animated-GIF support via coil3-gif on iOS — currently renders the first frame.
@Composable
fun LoaderWhiteImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.loader_white),
        contentDescription = "Loading Animation",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

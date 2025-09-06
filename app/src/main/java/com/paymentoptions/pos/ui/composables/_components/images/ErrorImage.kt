package com.paymentoptions.pos.ui.composables._components.images

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paymentoptions.pos.R

@Composable
fun ErrorImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.error_icon),
        contentDescription = "Credit Card",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Preview
@Composable
fun ErrorImagePreview() {
    ErrorImage(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    )
}
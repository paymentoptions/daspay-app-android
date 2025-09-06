package com.paymentoptions.pos.ui.composables._components.images.loaders

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.paymentoptions.pos.R

@Composable
fun LoaderImage(modifier: Modifier = Modifier) {
    AsyncImage(
        model = R.drawable.loader,
        contentDescription = "Loading Animation",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Preview
@Composable
fun LoaderImagePreview() {
    LoaderImage(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    )
}
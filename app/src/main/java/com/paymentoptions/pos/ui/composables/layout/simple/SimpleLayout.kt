package com.paymentoptions.pos.ui.composables.layout.simple

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.paymentoptions.pos.ui.composables._components.images.BackgroundImage

@Composable
fun SimpleLayout(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxSize()) {
        BackgroundImage(modifier = Modifier.fillMaxSize())
        content()
    }
}
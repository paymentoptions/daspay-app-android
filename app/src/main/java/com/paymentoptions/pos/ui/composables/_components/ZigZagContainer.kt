package com.paymentoptions.pos.ui.composables._components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.paymentoptions.pos.R

@Composable
fun ZigZagContainer(modifier: Modifier = Modifier, content: @Composable () -> Unit = {}) {
    Column(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.zigzag),
            contentDescription = "zig zag background",
            modifier = Modifier.fillMaxWidth()
        )
        content()
    }
}
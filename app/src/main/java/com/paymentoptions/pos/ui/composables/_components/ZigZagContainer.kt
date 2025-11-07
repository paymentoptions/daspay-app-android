package com.paymentoptions.pos.ui.composables._components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.paymentoptions.pos.R
import com.paymentoptions.pos.ui.theme.primary600

@Composable
fun ZigZagContainer1(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit = {},
) {
    return if (enabled)
        Column(modifier = modifier) {
            Image(
                painter = painterResource(id = R.drawable.zigzag),
                contentDescription = "zig zag background 1",
                modifier = Modifier.fillMaxWidth(),
            )
            content()
        }
    else content()
}

@Composable
fun ZigZagContainer2(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit = {},
) {
    return if (enabled)
        Column(modifier = modifier) {
            Image(
                painter = painterResource(id = R.drawable.zigzag),
                contentDescription = "zig zag background 2",
                modifier = Modifier
                    .fillMaxWidth()
                    .rotate(180f),
                colorFilter = ColorFilter.tint(primary600),
                contentScale = ContentScale.FillWidth
            )
            content()
        }
    else content()
}
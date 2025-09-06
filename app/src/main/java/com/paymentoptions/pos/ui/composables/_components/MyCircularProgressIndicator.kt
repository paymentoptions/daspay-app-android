package com.paymentoptions.pos.ui.composables._components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paymentoptions.pos.ui.composables._components.images.loaders.LoaderImage
import com.paymentoptions.pos.ui.theme.primary100

@Composable
fun MyCircularProgressIndicator(text: String? = null, color: Color = primary100) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        LoaderImage(
            modifier = Modifier.height(72.dp)
        )
//        CircularProgressIndicator(
//            modifier = Modifier.height(36.dp),
//            color = color,
//            gapSize = 0.dp,
//            strokeWidth = 3.dp,
//        )

        if (text != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text, color = Color.White)
        }
    }
}

@Preview
@Composable
fun MyCircularProgressIndicatorPreview() {
    MyCircularProgressIndicator()
}
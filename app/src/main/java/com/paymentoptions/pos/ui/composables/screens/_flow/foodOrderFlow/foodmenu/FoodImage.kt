package com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.foodmenu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.green500
import com.paymentoptions.pos.ui.theme.red500


@Composable
fun FoodImage(
    name: String,
    imageUrl: String,
    isVegetarian: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(50))
                .border(borderThin, shape = RoundedCornerShape(50))
                .zIndex(1f)
        )

        if (isVegetarian) Box(
            modifier = Modifier
//                .offset(x = (-5).dp)
                .size(16.dp)
                .border(
                    BorderStroke(1.dp, if (isVegetarian) green500 else red500),
                    shape = RoundedCornerShape(6.dp)
                )
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .padding(2.dp)
                .align(alignment = Alignment.TopStart)
                .zIndex(2f),
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (isVegetarian) green500 else red500, shape = RoundedCornerShape(50)
                    )
                    .clip(RoundedCornerShape(50))
            ) {}

        }
    }
}
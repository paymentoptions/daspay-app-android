package com.paymentoptions.pos.ui.screens._flow.foodOrderFlow.foodmenu
import paymentoptionspos.shared.generated.resources.veg_indicator
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.paymentoptions.pos.ui.theme.borderThin


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
            contentDescription = name,
            contentScale = ContentScale.Crop, // 🔥 IMPORTANT
            modifier = Modifier
                .fillMaxSize()                  // 🔥 MUST be square
                .clip(CircleShape)            // 🔥 Clip AFTER size
                .border(borderThin, CircleShape)
                .zIndex(1f)
        )

        if (isVegetarian) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .align(alignment = Alignment.TopStart)
                    .zIndex(2f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.veg_indicator),
                    contentDescription = "Vegetarian",
                    tint = Color.Unspecified
                )
            }
        } /*else Box(
            modifier = Modifier
                .size(16.dp)
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .border(
                    BorderStroke(1.dp, red500),
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(3.dp)
                .align(alignment = Alignment.TopStart)
                .zIndex(2f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .background(red500, shape = RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
            ) {}

        }*/
    }
}
package com.paymentoptions.pos.ui.composables.screens.foodorderflow.foodmenu

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paymentoptions.pos.ui.theme.bannerBgColor
import com.paymentoptions.pos.ui.theme.borderThin

@Composable
fun CartIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(borderThin, shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ShoppingCart,
            contentDescription = "Cart",
            tint = bannerBgColor,
            modifier = Modifier.fillMaxSize()
        )
    }
}

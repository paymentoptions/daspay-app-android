package com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.foodmenu

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.Cart
import com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.FoodOrderFlowStage
import com.paymentoptions.pos.ui.theme.bannerBgColor
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary900

@Composable
fun CartIcon(
    cartState: Cart, modifier: Modifier = Modifier, updateFlowStage: (FoodOrderFlowStage) -> Unit,
) {

    if (cartState.itemQuantity > 0) BadgedBox(
        modifier = modifier
            .border(
                borderThin, shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
            .clickable(enabled = cartState.itemQuantity > 0) {
                updateFlowStage(FoodOrderFlowStage.REVIEW_CART)
            }, badge = {
            Badge(
                modifier = Modifier.border(borderThin, shape = RoundedCornerShape(50)),
                containerColor = primary100.copy(alpha = 0.2f)
            ) {
                Text(text = cartState.itemQuantity.toString(), color = primary900)
            }
        }) {
        Icon(
            imageVector = Icons.Outlined.ShoppingCart,
            contentDescription = "Cart",
            tint = bannerBgColor,
            modifier = Modifier.fillMaxSize()
        )
    }
}

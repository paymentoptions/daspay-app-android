package com.paymentoptions.pos.ui.composables.screens._flow.foodorder.foodmenu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.FoodItem
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.utils.formatToPrecisionString

@Composable
fun FoodDetail(foodItem: FoodItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                foodItem.item.ProductCode.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = primary500
            )

            Text(
                foodItem.item.Currency,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = primary500.copy(alpha = 0.5f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                foodItem.item.ProductName,
                fontSize = 14.sp,
                fontWeight = FontWeight(980),
                color = primary500,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                "+${foodItem.item.ProductPrice.formatToPrecisionString()}",
                fontSize = 14.sp,
                fontWeight = FontWeight(980),
                color = primary500
            )
        }
    }
}
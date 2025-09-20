package com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.foodmenu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.FoodItem
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.utils.formatToPrecisionString

@Composable
fun FoodDetail(foodItem: FoodItem, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                foodItem.item.ProductSize.lowercase().capitalize(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = primary500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                foodItem.item.ProductName,
                fontSize = 14.sp,
                fontWeight = FontWeight(980),
                color = primary500,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                foodItem.item.Currency,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = primary500.copy(alpha = 0.5f)
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
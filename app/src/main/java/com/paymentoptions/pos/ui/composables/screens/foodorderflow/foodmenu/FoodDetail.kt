package com.paymentoptions.pos.ui.composables.screens.foodorderflow.foodmenu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.services.apiService.ProductListDataRecord
import com.paymentoptions.pos.ui.theme.primary500

@Composable
fun FoodDetail(foodItem: ProductListDataRecord, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                foodItem.ProductStock.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = primary500
            )

            Text(
                foodItem.Currency,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = primary500.copy(alpha = 0.5f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                foodItem.ProductName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = primary500
            )

            Text(
                "+ ${foodItem.ProductPrice}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = primary500
            )
        }
    }
}
package com.paymentoptions.pos.ui.composables.screens.foodorderflow.foodmenu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.services.apiService.CategoryListDataRecord
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary600
import com.paymentoptions.pos.ui.theme.purple50


@Composable
fun FoodCategories(
    foodCategories: List<CategoryListDataRecord>,
    selectedIndex: Int,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    //Food Categories
    LazyRow(
        modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        items(foodCategories.size) {

            val isSelected = selectedIndex == it

            SuggestionChip(
                border = if (isSelected) BorderStroke(
                    2.dp, primary100.copy(alpha = 0.2f)
                ) else BorderStroke(0.dp, Color.Transparent),
                colors = SuggestionChipDefaults.suggestionChipColors().copy(
                    containerColor = if (isSelected) Color.White else primary100.copy(alpha = 0.15f)
                ),
                onClick = { onClick(it) },
                label = {
                    Text(
                        text = foodCategories[it].CategoryName,
                        color = if (isSelected) primary600 else purple50,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                })
        }
    }
}
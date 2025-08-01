package com.paymentoptions.pos.ui.composables.screens._flow.foodorder.foodmenu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary600
import com.paymentoptions.pos.ui.theme.purple50

@Composable
fun FoodCategories(
    foodCategories: List<CategoryListDataRecord>,
    selectedFoodCategory: CategoryListDataRecord?,
    onClick: (CategoryListDataRecord) -> Unit,
    modifier: Modifier = Modifier,
) {
    //Food Categories
    Row(
        modifier = modifier
            .padding(end = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
            .horizontalScroll(state = rememberScrollState(), enabled = true),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        foodCategories.forEach {
            val isSelected = selectedFoodCategory == it

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
                        text = it.CategoryName,
                        color = if (isSelected) primary600 else purple50,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                })
        }
    }
}
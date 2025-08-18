package com.paymentoptions.pos.ui.composables.screens._flow.foodorder.foodmenu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.services.apiService.CategoryListDataRecord
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.theme.borderColor
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.primary600
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.utils.modifiers.conditional
import com.paymentoptions.pos.utils.modifiers.noRippleClickable

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

            Box(
                modifier = Modifier
                    .conditional(isSelected) {
                        shadow(
                            14.dp, spotColor = borderColor, shape = RoundedCornerShape(10.dp)
                        ).border(borderThin, shape = RoundedCornerShape(10.dp))
                    }
                    .background(
                        if (isSelected) Color.White else purple50.copy(
                            alpha = 0.05f
                        ), shape = RoundedCornerShape(10.dp)
                    )
                    .padding(10.dp)
                    .noRippleClickable(enabled = !isSelected) { onClick(it) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = it.CategoryName,
                    color = if (isSelected) primary600 else purple50,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
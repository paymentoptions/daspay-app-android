package com.paymentoptions.pos.ui.composables.screens._flow.foodorder.foodmenu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.CategoryListDataRecord
import com.paymentoptions.pos.services.apiService.ProductListDataRecord
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.MyCircularProgressIndicator
import com.paymentoptions.pos.ui.composables._components.NoData
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.inputs.SearchInput
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.Cart
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.FoodItem
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.FoodOrderFlowStage
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.utils.formatToPrecisionString
import com.paymentoptions.pos.utils.modifiers.conditional


fun searchLogic(foodItem: ProductListDataRecord, searchTerm: String): Boolean {
    return if (searchTerm.isEmpty()) true else {
        val searchInString = foodItem.ProductName + " " + foodItem.CategoryID
        searchInString.lowercase().contains(searchTerm)
    }
}

@Composable
fun FoodMenuBottomSectionContent(
    navController: NavController,
    enableScrolling: Boolean = false,
    foodCategoriesAvailable: Boolean,
    foodCategories: List<CategoryListDataRecord>,
    selectedFoodCategory: CategoryListDataRecord?,
    updateSelectedFoodCategory: (CategoryListDataRecord) -> Unit,
    foodItemsAvailable: Boolean,
    cartState: Cart,
    updateCartSate: (Cart) -> Unit,
    updateFlowStage: (FoodOrderFlowStage) -> Unit,
) {
    val scrollState = rememberScrollState()
    var searchState = rememberTextFieldState()
    var cartItemQuanitityState = remember {
        derivedStateOf { cartState.itemQuantity }
    }

    if (cartItemQuanitityState.value > -1) Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(10.dp))

        //Search bar & Cart
        Row(
            modifier = Modifier
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .fillMaxWidth()
                .height(52.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SearchInput(
                state = searchState, modifier = Modifier
                    .weight(4f)
                    .fillMaxHeight(), maxLength = 20
            )

            CartIcon(
                cartState = cartState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                updateFlowStage = updateFlowStage
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!foodCategoriesAvailable) MyCircularProgressIndicator(text = "Loading food categories...")
        else if (foodCategories.isEmpty()) NoData(text = " No food categories available") else FoodCategories(
            foodCategories = foodCategories,
            selectedFoodCategory = selectedFoodCategory,
            onClick = {
                updateSelectedFoodCategory(it)
            },
            modifier = Modifier
                .padding(start = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .fillMaxWidth()
                .height(40.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        //Food item List
        Column(
            modifier = Modifier
                .padding(
                    horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
                )
                .conditional(enableScrolling) {
                    weight(1f).verticalScroll(scrollState)
                }
                .fillMaxWidth()) {

            val foodItemsInCategory =
                cartState.foodItemMapByCategoryId[selectedFoodCategory?.CategoryID]
                    ?: listOf<FoodItem>()

            val filteredFoodItems = foodItemsInCategory.filter { foodItem ->
                searchLogic(
                    foodItem.item, searchState.text.toString()
                )
            }

            if (!foodItemsAvailable) MyCircularProgressIndicator() else if (filteredFoodItems.isEmpty()) NoData(
                text = if (foodItemsInCategory.isEmpty()) "No food items found in this category" else "No food items found matching your search"
            )
            else filteredFoodItems.forEachIndexed { index, foodItem ->
                FoodSummary(
                    foodItem, cartState, updateCartSate = { updateCartSate(cartState) })

                if (index + 1 < filteredFoodItems.size) HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = Color.LightGray.copy(alpha = 0.2f),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        //Cart details
        Column(
            modifier = Modifier
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Quantity", fontSize = 12.sp, fontWeight = FontWeight.Normal, color = primary500
                )

                Text(
                    cartState.itemQuantity.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = primary500
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(), color = Color.LightGray.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primary500
                )

                CurrencyText(
                    currency = "HKD",
                    amount = cartState.itemTotal.formatToPrecisionString(),
                    fontSize = 16.sp,
                    color = primary500
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            FilledButton(
                disabled = cartState.itemQuantity <= 0,
                text = "Review and Confirm",
                onClick = { updateFlowStage(FoodOrderFlowStage.REVIEW_CART) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(59.dp)
            )
        }
    }
}

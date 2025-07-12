package com.paymentoptions.pos.ui.composables.screens.foodmenu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.theme.borderThick
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary600
import com.paymentoptions.pos.ui.theme.purple50

data class FoodItem(
    val id: String,
    val name: String,
    val imageUrl: String,
    val size: String,
    val currency: String,
    val price: Float,
    val isVegetarian: Boolean = false,
    var cartQuantity: Int = 0,
)

data class Cart(
    val foodItem: FoodItem,
    val timestampInMilliseconds: Long,
    val quantity: Int = 0,
)

@Composable
fun BottomSectionContent(navController: NavController) {

    val foodCategories = listOf("Beverages", "Burgers & Fries", "Cake", "Pizza & Pasta")
    var selectedFoodCategoryIndex by remember { mutableIntStateOf(0) }
    var foodItems = remember { mutableListOf<FoodItem>() }
    var searchTerm by remember { mutableStateOf("") }
    val maxQuantityPerFoodItem = 4

    var totalCartQuantity by remember { mutableIntStateOf(0) }
    var totalCartPrice by remember { mutableFloatStateOf(0.00f) }

    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            SearchBar(
                searchTerm = searchTerm,
                onChange = { searchTerm = it },
                modifier = Modifier.weight(3f),
            )

            CartIcon()
        }

        Spacer(modifier = Modifier.height(10.dp))

        FoodCategories(
            foodCategories = foodCategories,
            selectedIndex = selectedFoodCategoryIndex,
            onClick = {
                selectedFoodCategoryIndex = it
            },
            modifier = Modifier
                .padding(
                    start = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP,
                    top = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP,
                    bottom = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
                )
                .fillMaxWidth()
                .height(40.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        //Food Item List
        Column(
            modifier = Modifier
                .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .fillMaxWidth()
        ) {

            foodItems.forEachIndexed { index, foodItem ->

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    //Image

                    //FoodItem Details
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                foodItem.size,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = primary500
                            )

                            Text(
                                foodItem.currency,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = primary500.copy(alpha = 0.5f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                foodItem.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = primary500
                            )

                            Text(
                                "+ ${foodItem.price}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = primary500
                            )
                        }
                    }

                    //Add to cart button
                    if (foodItem.cartQuantity == 0) {
                        FilterChip(
                            border = borderThick,
                            colors = FilterChipDefaults.filterChipColors().copy(
                                containerColor = Color.White
                            ),
                            onClick = {
                                foodItem.cartQuantity++
                                totalCartQuantity++
                                totalCartPrice += foodItem.price
                            },
                            selected = true,
                            leadingIcon = {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            },
                            label = {
                                Text(
                                    text = "Add",
                                    color = primary500,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            })
                    } else {
                        FilterChip(
                            border = borderThick,
                            colors = FilterChipDefaults.filterChipColors().copy(
                                containerColor = primary500, selectedLabelColor = Color.White
                            ),
                            onClick = { },
                            selected = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Remove,
                                    tint = Color.White,
                                    contentDescription = "Remove",
                                    modifier = Modifier.clickable {
                                        if (foodItem.cartQuantity > 0) {
                                            foodItem.cartQuantity--
                                            totalCartQuantity--
                                            totalCartPrice -= foodItem.price
                                        }
                                    })
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Add,
                                    tint = Color.White,
                                    contentDescription = "Add",
                                    modifier = Modifier.clickable {
                                        if (foodItem.cartQuantity < maxQuantityPerFoodItem) {
                                            foodItem.cartQuantity++
                                            totalCartQuantity++
                                            totalCartPrice += foodItem.price
                                        }
                                    })
                            },
                            label = {
                                Text(
                                    text = foodItem.cartQuantity.toString(),
                                    color = primary500,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            })
                    }
                }

                if (index + 1 < foodItems.size) HorizontalDivider(
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f))
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        //Cart details
        Column(
            modifier = Modifier
                .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Quantity", fontSize = 12.sp, fontWeight = FontWeight.Normal, color = primary500
                )

                Text(
                    totalCartQuantity.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = primary500
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.2f))
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
                    amount = totalCartPrice.toString(),
                    fontSize = 16.sp,
                    color = primary500
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            FilledButton(
                text = "Review and Confirm",
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(59.dp)
            )
        }

    }
}


@Composable
fun SearchBar(searchTerm: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {


}

@Composable
fun CartIcon(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Outlined.ShoppingCart,
        contentDescription = "Cart",
        modifier = Modifier
            .size(60.dp)
            .border(2.dp, primary100.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
            .padding(10.dp)

    )
}

@Composable
fun FoodCategories(
    foodCategories: List<String>,
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
                        text = foodCategories[it],
                        color = if (isSelected) primary600 else purple50,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                })
        }
    }
}
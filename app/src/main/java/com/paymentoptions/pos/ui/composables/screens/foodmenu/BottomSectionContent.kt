package com.paymentoptions.pos.ui.composables.screens.foodmenu

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import co.yml.charts.common.extensions.formatToSinglePrecision
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.inputs.SearchInput
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.theme.bannerBgColor
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.enabledFilledButtonGradientBrush
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary600
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.utils.modifiers.conditional

enum class Size {
    SMALL, REGULAR, MEDIUM, LARGE,
}

data class FoodItem(
    val id: String,
    val name: String,
    val imageUrl: String = "data:image/jpeg;base64,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",
    val size: Size,
    val currency: String,
    val price: Float,
    val isVegetarian: Boolean = false,
    var cartQuantity: Int = 0,
    var category: String,
)

val cappuccinoSmall = FoodItem(
    id = "1", name = "Cappuccino", size = Size.SMALL, currency = "HKD", price = 2.8f,
    isVegetarian = true,
    category = "Beverage",
)

val cappuccinoRegular = FoodItem(
    id = "1", name = "Cappuccino", size = Size.REGULAR, currency = "HKD", price = 3.8f,
    isVegetarian = true,
    category = "Beverage",
)

val cappuccinoMedium = FoodItem(
    id = "1", name = "Cappuccino", size = Size.MEDIUM, currency = "HKD", price = 5.8f,
    isVegetarian = false,
    category = "Beverage",
)

val cappuccinoLarge = FoodItem(
    id = "1", name = "Cappuccino", size = Size.LARGE, currency = "HKD", price = 8.0f,
    isVegetarian = false,
    category = "Beverage",
)

data class Cart(
    val foodItem: FoodItem,
    val timestampInMilliseconds: Long,
    val quantity: Int = 0,
)


@Composable
fun BottomSectionContent(navController: NavController, enableScrolling: Boolean = false) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val foodCategories = listOf("Beverages", "Burgers & Fries", "Cake", "Pizza & Pasta")
    var selectedFoodCategoryIndex by remember { mutableIntStateOf(0) }
    var foodItems = remember {
        listOf<FoodItem>(
            cappuccinoSmall,
            cappuccinoMedium,
            cappuccinoRegular,
            cappuccinoLarge
        )
    }
    var searchState = rememberTextFieldState()
    val maxQuantityPerFoodItem = 4
    var totalCartQuantity by remember { mutableIntStateOf(0) }
    var totalCartPrice by remember { mutableFloatStateOf(0.00f) }

    fun searchLogic(foodItem: FoodItem, searchTerm: String): Boolean {
        return if (searchState.text.isEmpty()) true else {
            val searchInString = foodItem.name + " " + foodItem.category + " " + foodItem.size
            searchInString.lowercase().contains(searchState.text)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3FAFF))
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
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        FoodCategories(
            foodCategories = foodCategories,
            selectedIndex = selectedFoodCategoryIndex,
            onClick = {
                selectedFoodCategoryIndex = it
            },
            modifier = Modifier
                .padding(start = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .fillMaxWidth()
                .height(40.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        //Food Item List
        Column(
            modifier = Modifier
                .padding(
                    horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
                )
                .conditional(enableScrolling) {
                    weight(1f).verticalScroll(scrollState)
                }
                .fillMaxWidth()) {
            foodItems.filter { foodItem -> searchLogic(foodItem, searchState.text.toString()) }
                .forEachIndexed { index, foodItem ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {

                        FoodImage(
                            foodItem.name,
                            foodItem.imageUrl,
                            foodItem.isVegetarian,
                            modifier = Modifier.size(44.dp)
                        )

                        FoodDetail(foodItem, modifier = Modifier.weight(1f))

                        //Add to cart button

                        Row(
                            modifier = Modifier
                                .width(80.dp)
                                .height(36.dp)
                                .conditional(foodItem.cartQuantity == 0) {
                                    background(
                                        Color.White, shape = RoundedCornerShape(10.dp)
                                    ).border(borderThin, shape = RoundedCornerShape(10.dp))
                                }
                                .conditional(foodItem.cartQuantity != 0) {
                                    background(
                                        brush = enabledFilledButtonGradientBrush,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                                .padding(6.dp), verticalAlignment = Alignment.CenterVertically) {

                            Icon(
                                imageVector = if (foodItem.cartQuantity == 0) Icons.Default.Add else Icons.Default.Remove,
                                tint = if (foodItem.cartQuantity == 0) primary500 else Color.White,
                                contentDescription = if (foodItem.cartQuantity == 0) "Add" else "Remove",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (foodItem.cartQuantity > 0) {
                                            foodItem.cartQuantity--
                                            totalCartQuantity--
                                            totalCartPrice -= foodItem.price
                                        }
                                    })

                            Text(
                                text = if (foodItem.cartQuantity == 0) "Add" else foodItem.cartQuantity.toString(),
                                color = if (foodItem.cartQuantity == 0) primary500 else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .weight(if (foodItem.cartQuantity == 0) 2.5f else 1.5f)
                                    .clickable {
                                        if (foodItem.cartQuantity < maxQuantityPerFoodItem) {
                                            foodItem.cartQuantity++
                                            totalCartQuantity++
                                            totalCartPrice += foodItem.price
                                        }
                                    },
                                textAlign = TextAlign.Center
                            )

                            if (foodItem.cartQuantity != 0) Icon(
                                Icons.Default.Add,
                                tint = Color.White,
                                contentDescription = "Add",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (foodItem.cartQuantity < maxQuantityPerFoodItem) {
                                            foodItem.cartQuantity++
                                            totalCartQuantity++
                                            totalCartPrice += foodItem.price
                                        }
                                    })
                        }
                    }

                    if (index + 1 < foodItems.size) HorizontalDivider(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .background(Color.White.copy(alpha = 0.8f)),

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
                    amount = totalCartPrice.formatToSinglePrecision(),
                    fontSize = 16.sp,
                    color = primary500
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            FilledButton(
                text = "Review and Confirm", onClick = {
                    Toast.makeText(context, "Under development", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier
                    .fillMaxWidth()
                    .height(59.dp)
            )
        }
    }
}

@Composable
fun FoodItem(foodItem: FoodItem, modifier: Modifier = Modifier) {
}


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
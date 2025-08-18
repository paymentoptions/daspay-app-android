package com.paymentoptions.pos.ui.composables.screens._flow.foodorder.foodmenu

import android.os.Handler
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.Cart
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.FoodItem
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.MAX_QUANTITY_PER_FOOD_ITEM
import com.paymentoptions.pos.ui.theme.borderColor
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.enabledFilledButtonGradientBrush
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.red300
import com.paymentoptions.pos.ui.theme.red500
import com.paymentoptions.pos.utils.modifiers.conditional

@Composable
fun FoodSummaryForReview(
    foodItem: FoodItem,
    longClickedFoodItem: FoodItem?,
    cartState: Cart,
    updateLongClickedFoodItem: (FoodItem?) -> Unit,
    updateCartSate: (Cart) -> Unit, createToast: (ToastData) -> Unit,
    setShowToast: (Boolean) -> Unit,
) {
    val context = LocalContext.current

    val borderRadius = 20.dp
    val haptics = LocalHapticFeedback.current
    var isLongClicked = longClickedFoodItem == foodItem

    var backPressHandled by remember { mutableStateOf(false) }

    BackHandler(enabled = !backPressHandled) {
        updateLongClickedFoodItem(null)
        backPressHandled = true
    }

    fun removeQuantity() {
        if (foodItem.cartQuantity > 0) {
            cartState.decreaseFoodItemQuantity(foodItem, context)
            updateCartSate(cartState)
        }

        createToast(
            ToastData(
                type = ToastType.ERROR,
                text = foodItem.item.ProductName + " removed",
                cartCount = foodItem.cartQuantity
            )
        )
        setShowToast(true)

        Handler().postDelayed({
            setShowToast(false)
        }, 1000)
    }

    fun addQuantity() {
        if (foodItem.cartQuantity < MAX_QUANTITY_PER_FOOD_ITEM) {
            cartState.increaseFoodItemQuantity(foodItem, context)
            updateCartSate(cartState)
        }

        createToast(
            ToastData(
                type = ToastType.SUCCESS,
                text = foodItem.item.ProductName + " added",
                cartCount = foodItem.cartQuantity
            )
        )
        setShowToast(true)

        Handler().postDelayed({
            setShowToast(false)
        }, 1000)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isLongClicked) 0.dp else DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
        horizontalArrangement = Arrangement.Absolute.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Card(
            colors = CardDefaults.cardColors().copy(containerColor = Color.White),
            border = borderThin,
            shape = RoundedCornerShape(
                topStart = if (isLongClicked) 0.dp else borderRadius,
                topEnd = borderRadius,
                bottomStart = if (isLongClicked) 0.dp else borderRadius,
                bottomEnd = borderRadius
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isLongClicked) 8.dp else 2.dp, shape = RoundedCornerShape(
                        topStart = if (isLongClicked) 0.dp else borderRadius,
                        topEnd = borderRadius,
                        bottomStart = if (isLongClicked) 0.dp else borderRadius,
                        bottomEnd = borderRadius
                    ), ambientColor = borderColor
                )
                .combinedClickable(onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)
                }, onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    updateLongClickedFoodItem(foodItem)
                })
                .weight(if (isLongClicked) 8f else 1f)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (isLongClicked) 12.dp else DEFAULT_BOTTOM_SECTION_PADDING_IN_DP,
                        vertical = 12.dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {

                if (!isLongClicked) FoodImage(
                    foodItem.item.ProductName,
                    foodItem.item.ProductImage,
                    foodItem.isVegetarian,
                    modifier = Modifier.size(44.dp),
                )

                FoodDetail(foodItem, modifier = Modifier.weight(1f))

                //Add to cart button
                Row(
                    modifier = Modifier
                        .width(80.dp)
                        .height(36.dp)
                        .conditional(foodItem.cartQuantity == 0) {
                            shadow(
                                14.dp, spotColor = borderColor, shape = RoundedCornerShape(10.dp)
                            )
                                .background(
                                    Color.White, shape = RoundedCornerShape(10.dp)
                                )
                                .border(borderThin, shape = RoundedCornerShape(10.dp))
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
                            .clickable(enabled = !(foodItem.cartQuantity == 1 && isLongClicked)) {
                                when (foodItem.cartQuantity) {
                                    0 -> {
                                        addQuantity()
                                    }

                                    1 -> {
                                        updateLongClickedFoodItem(foodItem)
                                    }

                                    else -> {
                                        updateLongClickedFoodItem(null)
                                        removeQuantity()
                                    }
                                }
                            })

                    Text(
                        text = if (foodItem.cartQuantity == 0) "Add" else foodItem.cartQuantity.toString(),
                        color = if (foodItem.cartQuantity == 0) primary500 else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .weight(if (foodItem.cartQuantity == 0) 2.5f else 1.5f)
                            .clickable(enabled = foodItem.cartQuantity == 0) {
                                if (foodItem.cartQuantity < MAX_QUANTITY_PER_FOOD_ITEM) {
                                    updateLongClickedFoodItem(null)
                                    addQuantity()
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
                            .clickable(enabled = foodItem.cartQuantity < MAX_QUANTITY_PER_FOOD_ITEM) {
                                if (foodItem.cartQuantity < MAX_QUANTITY_PER_FOOD_ITEM) {
                                    updateLongClickedFoodItem(null)
                                    addQuantity()
                                }
                            })
                }
            }
        }

        if (isLongClicked) Icon(
            Icons.Outlined.Delete,
            tint = red500,
            contentDescription = "Delete",
            modifier = Modifier
                .padding(end = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .background(
                    red300.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
                .aspectRatio(1f)
                .weight(2f)
                .clickable {
                    updateLongClickedFoodItem(null)
                    removeQuantity()
                })
    }
}
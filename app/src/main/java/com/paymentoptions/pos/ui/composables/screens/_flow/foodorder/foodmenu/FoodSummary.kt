package com.paymentoptions.pos.ui.composables.screens._flow.foodorder.foodmenu

import android.os.Handler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.Cart
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.FoodItem
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.MAX_QUANTITY_PER_FOOD_ITEM
import com.paymentoptions.pos.ui.theme.borderColor
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.enabledFilledButtonGradientBrush
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.utils.modifiers.conditional

@Composable
fun FoodSummary(
    foodItem: FoodItem,
    cartState: Cart,
    updateCartSate: (Cart) -> Unit,
    createToast: (ToastData) -> Unit,
    setShowToast: (Boolean) -> Unit,
) {
    val context = LocalContext.current

    fun removeQuantity() {
        if (foodItem.cartQuantity > 0) {
            cartState.decreaseFoodItemQuantity(foodItem, context)
            updateCartSate(cartState)

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
    }

    fun addQuantity() {
        if (foodItem.cartQuantity < MAX_QUANTITY_PER_FOOD_ITEM) {
            cartState.increaseFoodItemQuantity(foodItem, context = context)
            updateCartSate(cartState)

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
            }, 1500)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        FoodImage(
            foodItem.item.ProductName,
            foodItem.item.ProductImage,
            foodItem.isVegetarian,
            modifier = Modifier.size(44.dp)
        )

        FoodDetail(foodItem, modifier = Modifier.weight(1f))

        //Add to cart button
        Row(
            modifier = Modifier
                .width(80.dp)
                .height(30.dp)
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
                        brush = enabledFilledButtonGradientBrush, shape = RoundedCornerShape(10.dp)
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
                        if (foodItem.cartQuantity == 0) addQuantity()
                        else removeQuantity()
                    })

            Text(
                text = if (foodItem.cartQuantity == 0) "ADD" else foodItem.cartQuantity.toString(),
                color = if (foodItem.cartQuantity == 0) primary500 else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .weight(if (foodItem.cartQuantity == 0) 2.5f else 1.5f)
                    .clickable(enabled = foodItem.cartQuantity == 0) { addQuantity() },
                textAlign = TextAlign.Center
            )

            if (foodItem.cartQuantity != 0) Icon(
                Icons.Default.Add,
                tint = Color.White,
                contentDescription = "Add",
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = foodItem.cartQuantity < MAX_QUANTITY_PER_FOOD_ITEM) { addQuantity() })
        }
    }
}
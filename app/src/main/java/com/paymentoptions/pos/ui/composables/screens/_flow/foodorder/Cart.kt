package com.paymentoptions.pos.ui.composables.screens._flow.foodorder

import android.content.Context
import co.yml.charts.common.extensions.isNotNull
import com.paymentoptions.pos.device.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class Cart(
    var foodItemMapByCategoryId: MutableMap<String, List<FoodItem>> = mutableMapOf<String, List<FoodItem>>(),
    var timestampInMilliseconds: Long? = null,
    var itemQuantity: Int = 0,
    var itemTotal: Float = 0.0f,
    var serviceChargePercentage: Float,
    var gstPercentage: Float,
    var additionalCharge: Float,
    var additionalAmountNote: String,
) {
    companion object {
        fun save(context: Context, cart: Cart) {
            SharedPreferences.saveCart(context, cart)
        }

        fun load(context: Context): Cart? {
            val cart = SharedPreferences.getCart(context)
            return cart
        }

        fun clearSavedCart(context: Context) {
            SharedPreferences.clearSavedCart(context)
        }
    }

    fun calculateServiceCharge() = itemTotal.times(serviceChargePercentage.div(100))
    fun calculateGstCharge() = itemTotal.times(gstPercentage.div(100))
    fun calculateGrandTotal() =
        itemTotal.plus(calculateServiceCharge()).plus(calculateGstCharge()).plus(additionalCharge)

    fun toJson(): String {
        return Json.encodeToString(this)
    }

    fun decreaseFoodItemQuantity(foodItem: FoodItem, context: Context) {
        if (foodItem.cartQuantity > 0) {
            foodItem.decreaseQuantity()
            this.itemQuantity--
            this.itemTotal -= foodItem.item.ProductPrice
            save(context, this)
        }
    }

    fun increaseFoodItemQuantity(foodItem: FoodItem, context: Context) {
        if (foodItem.cartQuantity < MAX_QUANTITY_PER_FOOD_ITEM) {
            foodItem.increaseQuantity()
            this.itemQuantity++
            this.itemTotal += foodItem.item.ProductPrice
            save(context, this)
        }
    }

    fun replaceFoodCategory(categoryId: String, newFoodItems: List<FoodItem>, context: Context) {
        val oldFoodItemsInTheCategory = this.foodItemMapByCategoryId[categoryId]
        val newFoodItemsInTheCategorySorted =
            newFoodItems.sortedBy { foodItem -> foodItem.item.CategoryID }

        if (oldFoodItemsInTheCategory.isNullOrEmpty()) this.foodItemMapByCategoryId[categoryId] =
            newFoodItemsInTheCategorySorted
        else {
            newFoodItemsInTheCategorySorted.forEach { newFoodItem ->
                val oldFoodItem =
                    oldFoodItemsInTheCategory.find { oldFoodItem -> oldFoodItem.item.ProductID == newFoodItem.item.ProductID }

                if (oldFoodItem.isNotNull()) newFoodItem.copyCartQuantity(oldFoodItem!!)
            }
            this.foodItemMapByCategoryId[categoryId] = newFoodItemsInTheCategorySorted
        }
        save(context, this)
    }

    fun getFoodItemsForReview(): List<FoodItem> {
        var foodItems: List<FoodItem> = listOf<FoodItem>()

        this.foodItemMapByCategoryId.forEach { categoryFoodItemMap ->
            categoryFoodItemMap.value.filter { foodItem -> foodItem.cartQuantity > 0 }
                .forEach { foodItem -> foodItems = foodItems.plus(foodItem) }
        }
        return foodItems
    }

    fun copy(): Cart {
        return Cart(
            foodItemMapByCategoryId = this.foodItemMapByCategoryId,
            serviceChargePercentage = this.serviceChargePercentage,
            gstPercentage = this.gstPercentage,
            additionalCharge = this.additionalCharge,
            timestampInMilliseconds = this.timestampInMilliseconds,
            itemQuantity = this.itemQuantity,
            itemTotal = this.itemTotal,
            additionalAmountNote = this.additionalAmountNote,
        )
    }

    override fun toString(): String {
        return toJson()
    }
}
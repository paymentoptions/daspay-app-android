package com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow

import android.content.Context
import co.yml.charts.common.extensions.isNotNull
import com.paymentoptions.pos.storage.AppStorage
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class Cart(
    var foodItemMapByCategoryId: MutableMap<String, List<FoodItem>> = mutableMapOf<String, List<FoodItem>>(),
    var timestampInMilliseconds: Long? = null,
    var itemQuantity: Int = 0,
    var itemTotal: Float = 0.0f,
    var serviceChargePercentage: Float = 10f,
    var gstPercentage: Float = 9f,
    var additionalCharge: Float = 0f,
    var additionalAmountNote: String = "",
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun save(context: Context, cart: Cart) {
            AppStorage.cartJson = cart.toJson()
        }

        fun load(context: Context): Cart? {
            return try {
                AppStorage.cartJson?.let { json.decodeFromString<Cart>(it) }
            } catch (e: Exception) {
                Cart()
            }
        }
    }

    fun calculateServiceCharge() = itemTotal.times(serviceChargePercentage.div(100))
    fun calculateGstCharge() = itemTotal.times(gstPercentage.div(100))
    fun calculateGrandTotal() =
        itemTotal.plus(calculateServiceCharge()).plus(calculateGstCharge()).plus(additionalCharge)

    fun toJson(): String {
        return Json.encodeToString(this)
    }

    fun clearSavedCart(context: Context) {
        foodItemMapByCategoryId.forEach {
            it.value.forEach { foodItem ->
                removeFoodItemQuantity(foodItem, context)
            }
        }
        additionalCharge = 0f
        additionalAmountNote = ""
        save(context, this)
    }

    fun removeFoodItemQuantity(foodItem: FoodItem, context: Context) {
        if (foodItem.cartQuantity > 0) {
            val previousSize = foodItem.cartQuantity
            foodItem.deleteQuantity()
            val newSize = itemQuantity - previousSize
            if(newSize > 0){
                this.itemQuantity = newSize
            } else {
                this.itemQuantity = 0
            }
            val newTotal = this.itemTotal - (foodItem.item.ProductPrice * previousSize)
            if(newTotal > 0){
                this.itemTotal = newTotal
            } else {
                this.itemTotal = 0f
            }
            save(context, this)
        }
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
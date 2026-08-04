package com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow

import android.content.Context
import co.yml.charts.common.extensions.isNotNull
import com.paymentoptions.pos.device.DPSharedPreferences
import com.paymentoptions.pos.services.apiService.MerchantSetting
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class Cart(
    var foodItemMapByCategoryId: MutableMap<String, List<FoodItem>> = mutableMapOf<String, List<FoodItem>>(),
    var timestampInMilliseconds: Long? = null,
    var itemQuantity: Int = 0,
    var itemTotal: Float = 0.0f,
    var additionalCharge: Float = 0f,
    var additionalAmountNote: String = "",
    var merchantSetting: MerchantSetting? = null,
    var serviceCharge: Float = 0f,
    var gstCharge: Float = 0f,
    var grandTotal: Float = 0f
) {
    companion object {
        fun save(context: Context, cart: Cart) {
            DPSharedPreferences.saveCart(context, cart)
        }

        fun load(context: Context): Cart? {
            try {
                val cart = DPSharedPreferences.getCart(context)
                if (cart != null && cart.merchantSetting == null) {
                    cart.merchantSetting = DPSharedPreferences.getMerchantSettings(context)
                }
                return cart
            } catch (e: Exception) {
                return Cart(merchantSetting = DPSharedPreferences.getMerchantSettings(context))
            }
        }
    }

    fun calculateServiceCharge(): Float {
        var totalService = 0f
        if(merchantSetting != null && merchantSetting!!.CatalogEnabled == true){
            foodItemMapByCategoryId.forEach { (_, items) ->
                items.forEach {
                    if (it.cartQuantity > 0) {
                        totalService += (it.item.ServiceFeeAmount ?: 0f) * it.cartQuantity
                    }
                }
            }
        }
        return totalService
    }
    fun calculateGstCharge(totalValue: Float): Float {
        if(merchantSetting != null && merchantSetting!!.CatalogEnabled == true && merchantSetting!!.TaxOnOtherFeesPerc!= null){
            return totalValue.times(merchantSetting!!.TaxOnOtherFeesPerc!!).div(100)
        } else {
            return 0f
        }

    }

    fun updateTotals() {
        serviceCharge = calculateServiceCharge()
        val totalBeforeTax = itemTotal.plus(serviceCharge).plus(additionalCharge)
        gstCharge = calculateGstCharge(totalBeforeTax)
        grandTotal = totalBeforeTax + gstCharge
    }


    fun toJson(): String {
        return Json.encodeToString(this)
    }

    fun updateAdditionalCharge(amount: Float, note: String, context: Context) {
        this.additionalCharge = amount
        this.additionalAmountNote = note
        updateTotals()
        save(context, this)
    }

    fun clearSavedCart(context: Context) {
        foodItemMapByCategoryId.forEach {
            it.value.forEach { foodItem ->
                removeFoodItemQuantity(foodItem, context)
            }
        }
        additionalCharge = 0f
        additionalAmountNote = ""
        updateTotals()
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
            updateTotals()
            save(context, this)
        }
    }

    fun decreaseFoodItemQuantity(foodItem: FoodItem, context: Context) {
        if (foodItem.cartQuantity > 0) {
            foodItem.decreaseQuantity()
            this.itemQuantity--
            this.itemTotal -= foodItem.item.ProductPrice
            updateTotals()
            save(context, this)
        }
    }

    fun increaseFoodItemQuantity(foodItem: FoodItem, context: Context) {
        if (foodItem.cartQuantity < MAX_QUANTITY_PER_FOOD_ITEM) {
            foodItem.increaseQuantity()
            this.itemQuantity++
            this.itemTotal += foodItem.item.ProductPrice
            updateTotals()
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
        updateTotals()
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
            additionalCharge = this.additionalCharge,
            timestampInMilliseconds = this.timestampInMilliseconds,
            itemQuantity = this.itemQuantity,
            itemTotal = this.itemTotal,
            additionalAmountNote = this.additionalAmountNote,
            merchantSetting = this.merchantSetting,
            serviceCharge = this.serviceCharge,
            gstCharge = this.gstCharge,
            grandTotal = this.grandTotal
        )
    }

    override fun toString(): String {
        return toJson()
    }
}
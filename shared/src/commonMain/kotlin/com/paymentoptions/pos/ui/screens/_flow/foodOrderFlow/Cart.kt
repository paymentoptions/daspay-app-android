package com.paymentoptions.pos.ui.screens._flow.foodOrderFlow

import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.network.MerchantSetting
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
    var additionalCharge: Float = 0f,
    var additionalAmountNote: String = "",
    var merchantSetting: MerchantSetting? = null,
    var serviceCharge: Float = 0f,
    var gstCharge: Float = 0f,
    var grandTotal: Float = 0f,
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun save( cart: Cart) {
            AppStorage.cartJson = cart.toJson()
        }

        /**
         * Loads the saved cart with the current merchant settings, and recalculates its totals so a
         * cart saved by an older version (or before the settings changed) never charges a stale amount.
         */
        fun load(): Cart? {
            val merchantSetting = DPStorageManager.getMerchantSettings()
            return try {
                AppStorage.cartJson?.let { json.decodeFromString<Cart>(it) }?.also { cart ->
                    cart.merchantSetting = merchantSetting ?: cart.merchantSetting
                    cart.updateTotals()
                }
            } catch (e: Exception) {
                Cart(merchantSetting = merchantSetting)
            }
        }
    }

    /** Sum of each item's fixed service fee times its quantity, for items with the fee enabled. */
    fun calculateServiceCharge(): Float {
        var totalService = 0f
        foodItemMapByCategoryId.forEach { (_, items) ->
            items.forEach {
                if (it.item.ServiceFeeEnabled == true && it.cartQuantity > 0) {
                    totalService += (it.item.ServiceFeeAmount ?: 0f) * it.cartQuantity
                }
            }
        }
        return totalService
    }

    /** Tax on [totalValue] at the merchant's rate; zero unless the merchant's catalog is enabled. */
    fun calculateGstCharge(totalValue: Float): Float {
        val setting = merchantSetting ?: return 0f
        val taxPercent = setting.TaxOnOtherFeesPerc ?: return 0f
        if (setting.CatalogEnabled != true) return 0f
        return totalValue * taxPercent / 100
    }

    /** Recomputes service charge, tax (on items + service + additional charge) and the grand total. */
    fun updateTotals() {
        serviceCharge = calculateServiceCharge()
        val totalBeforeTax = itemTotal + serviceCharge + additionalCharge
        gstCharge = calculateGstCharge(totalBeforeTax)
        grandTotal = totalBeforeTax + gstCharge
    }

    fun updateAdditionalCharge(amount: Float, note: String) {
        additionalCharge = amount
        additionalAmountNote = note
        updateTotals()
        save(this)
    }

    fun toJson(): String {
        return Json.encodeToString(this)
    }

    fun clearSavedCart() {
        foodItemMapByCategoryId.forEach {
            it.value.forEach { foodItem ->
                removeFoodItemQuantity(foodItem)
            }
        }
        additionalCharge = 0f
        additionalAmountNote = ""
        updateTotals()
        save(this)
    }

    fun removeFoodItemQuantity(foodItem: FoodItem) {
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
            save(this)
        }
    }

    fun decreaseFoodItemQuantity(foodItem: FoodItem) {
        if (foodItem.cartQuantity > 0) {
            foodItem.decreaseQuantity()
            this.itemQuantity--
            this.itemTotal -= foodItem.item.ProductPrice
            updateTotals()
            save(this)
        }
    }

    fun increaseFoodItemQuantity(foodItem: FoodItem) {
        if (foodItem.cartQuantity < MAX_QUANTITY_PER_FOOD_ITEM) {
            foodItem.increaseQuantity()
            this.itemQuantity++
            this.itemTotal += foodItem.item.ProductPrice
            updateTotals()
            save(this)
        }
    }

    fun replaceFoodCategory(categoryId: String, newFoodItems: List<FoodItem>) {
        val oldFoodItemsInTheCategory = this.foodItemMapByCategoryId[categoryId]
        val newFoodItemsInTheCategorySorted =
            newFoodItems.sortedBy { foodItem -> foodItem.item.CategoryID }

        if (oldFoodItemsInTheCategory.isNullOrEmpty()) this.foodItemMapByCategoryId[categoryId] =
            newFoodItemsInTheCategorySorted
        else {
            newFoodItemsInTheCategorySorted.forEach { newFoodItem ->
                val oldFoodItem =
                    oldFoodItemsInTheCategory.find { oldFoodItem -> oldFoodItem.item.ProductID == newFoodItem.item.ProductID }

                if (oldFoodItem != null) newFoodItem.copyCartQuantity(oldFoodItem!!)
            }
            this.foodItemMapByCategoryId[categoryId] = newFoodItemsInTheCategorySorted
        }
        updateTotals()
        save(this)
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
            grandTotal = this.grandTotal,
        )
    }

    override fun toString(): String {
        return toJson()
    }
}
package com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow

import co.yml.charts.common.extensions.isNotNull
import com.paymentoptions.pos.services.apiService.ProductListDataRecord
import kotlinx.serialization.Serializable

@Serializable
class FoodItem(
    val item: ProductListDataRecord,
    var cartQuantity: Int = 0,
) {
    fun decreaseQuantity() {
        this.cartQuantity--
    }

    fun increaseQuantity() {
        this.cartQuantity++
    }

    fun copyCartQuantity(f: FoodItem) {
        this.cartQuantity = f.cartQuantity
    }

    fun isNonVeg(): Boolean {
        return this.item.ProductFoodType == "NONVEG"
    }

    fun imageUrl(): String {
        return if (this.item.ProductImage.isNotNull()) this.item.ProductImage!! else ""
    }

    override fun toString(): String {
        return this.item.ProductName + ": " + this.cartQuantity
    }
}
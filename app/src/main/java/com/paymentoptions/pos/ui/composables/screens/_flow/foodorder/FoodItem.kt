package com.paymentoptions.pos.ui.composables.screens._flow.foodorder

import com.paymentoptions.pos.services.apiService.ProductListDataRecord


class FoodItem(
    val item: ProductListDataRecord,
    val isVegetarian: Boolean = true,
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

    override fun toString(): String {
        return this.item.ProductName + ": " + this.cartQuantity
    }
}
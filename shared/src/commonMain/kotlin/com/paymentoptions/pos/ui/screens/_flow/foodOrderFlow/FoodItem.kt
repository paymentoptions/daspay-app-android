package com.paymentoptions.pos.ui.screens._flow.foodOrderFlow

import com.paymentoptions.pos.network.ProductListDataRecord
import kotlinx.serialization.Serializable

@Serializable
class FoodItem(
    val item: ProductListDataRecord,
    var cartQuantity: Int = 0,
) {
    fun decreaseQuantity() {
        this.cartQuantity--
    }

    fun deleteQuantity() {
        this.cartQuantity = 0
    }

    fun increaseQuantity() {
        this.cartQuantity++
    }

    fun copyCartQuantity(f: FoodItem) {
        this.cartQuantity = f.cartQuantity
    }

//    fun isNonVeg(): Boolean {
//        return this.item.ProductFoodType == "NONVEG"
//    }

    fun imageUrl(): String {
        return this.item.ProductImage ?: ""
    }

    override fun toString(): String {
        return this.item.ProductName + ": " + this.cartQuantity
    }
}
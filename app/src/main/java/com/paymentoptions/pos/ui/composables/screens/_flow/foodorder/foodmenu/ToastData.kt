package com.paymentoptions.pos.ui.composables.screens._flow.foodorder.foodmenu


class ToastData(
    var cartCount: Int = 0,
    var text: String = "",
    var type: ToastType? = null,
    var alpha: Float = 1f,
) {
    fun setToast(newToastData: ToastData) {
        this.cartCount = newToastData.cartCount
        this.type = newToastData.type
        this.text = newToastData.text
        this.alpha = newToastData.alpha
    }
}
package com.paymentoptions.pos.ui.composables._components.mytoast

class ToastData(
    var text: String = "",
    var type: ToastType? = null,
    var alpha: Float = 1f,
) {
    fun setToast(newToastData: ToastData) {
        this.type = newToastData.type
        this.text = newToastData.text
        this.alpha = newToastData.alpha
    }
}
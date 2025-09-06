package com.paymentoptions.pos.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.TapAndPlay
import androidx.compose.ui.graphics.vector.ImageVector

class PaymentMethod(
    val text: String,
    val icon: ImageVector,
    var isEnabled: Boolean = true,
) {
    fun setIsEnabled(isEnabled: Boolean) {
        this.isEnabled = isEnabled
    }
}

val tapPaymentMethod = PaymentMethod(text = "Tap", icon = Icons.Default.TapAndPlay)
val qrCodePaymentMethod = PaymentMethod(text = "QR Code", icon = Icons.Default.QrCode)
val cashPaymentMethod = PaymentMethod(text = "Cash", icon = Icons.Default.Money, isEnabled = false)
val viaLinkPaymentMethod = PaymentMethod(text = "Via Link", icon = Icons.Default.Link)

val paymentMethods = listOf(
    tapPaymentMethod, qrCodePaymentMethod, cashPaymentMethod, viaLinkPaymentMethod
)

package com.paymentoptions.pos.utils

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.TapAndPlay
import androidx.compose.ui.graphics.vector.ImageVector
import com.paymentoptions.pos.device.DPSharedPreferences

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


fun paymentMethods(context: Context): List<PaymentMethod> {
    return DPSharedPreferences.getAvailablePaymentsList(context)
}

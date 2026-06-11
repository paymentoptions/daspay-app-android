package com.paymentoptions.pos.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.TapAndPlay
import androidx.compose.ui.graphics.vector.ImageVector
import com.paymentoptions.pos.network.ExternalConfigurationResponse
import com.paymentoptions.pos.storage.AppStorage
import kotlinx.serialization.json.Json

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

private val paymentJson = Json { ignoreUnknownKeys = true }

fun paymentMethods(): List<PaymentMethod> {
    val config = AppStorage.deviceConfigJson?.let {
        paymentJson.decodeFromString<ExternalConfigurationResponse>(it)
    } ?: return emptyList()
    val availableTypes = config.data?.paymentMethod?.map { it.Type }?.toSet() ?: emptySet()
    return buildList {
        if ("SOFTPOS" in availableTypes) add(tapPaymentMethod)
        if ("QR" in availableTypes) add(qrCodePaymentMethod)
        if ("PBL" in availableTypes) add(viaLinkPaymentMethod)
    }
}

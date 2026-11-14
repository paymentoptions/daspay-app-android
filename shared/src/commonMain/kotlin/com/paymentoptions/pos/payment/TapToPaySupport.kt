package com.paymentoptions.pos.payment

import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.endpoints.payByLink
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.ui.screens._flow.foodOrderFlow.Cart
import com.paymentoptions.pos.ui.screens._flow.foodOrderFlow.getPayByLinkRequest
import kotlin.coroutines.cancellation.CancellationException

/**
 * For a food-order Tap to Pay payment: registers the cart as a pay-by-link product so the payment
 * carries its line items, and returns the product ID to send as the payment's cartID. Clears the
 * saved cart once the product exists. Returns "" when there is no cart or the call fails; the
 * payment then goes ahead without line items (release behaviour).
 */
suspend fun createCartProductId(cart: Cart?, currency: String): String {
    if (cart == null) return ""
    return try {
        val request = getPayByLinkRequest("Tap to Pay ", cart, currency)
        val response = payByLink(dasmid = DPStorageManager.getTapPayDasmid(), request = request)
        val productId = response?.data?.ProductID.orEmpty()
        AppLogger.debug("TapToPay cart product created: productIdPresent=${productId.isNotBlank()}")
        AppStorage.cartJson = null
        productId
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AppLogger.error("TapToPay could not create cart product; continuing without cartID: ${e.message}")
        ""
    }
}

/**
 * True when the amount is at or above the merchant's CVM limit, so the cardholder must sign.
 * Accepts amounts with thousands separators.
 */
fun requiresMandatorySignature(amountToCharge: String): Boolean {
    val amount = amountToCharge.replace(",", "").toDoubleOrNull() ?: return false
    return amount >= DPStorageManager.getCvmLimit()
}

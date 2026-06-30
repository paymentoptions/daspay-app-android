package com.paymentoptions.pos.payment

import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.getDeviceIpAddress
import com.paymentoptions.pos.network.Address
import com.paymentoptions.pos.network.PaymentMethodRequest
import com.paymentoptions.pos.network.PaymentRequest
import com.paymentoptions.pos.network.PaymentReturnUrl
import com.paymentoptions.pos.network.endpoints.payment
import com.paymentoptions.pos.utils.decodeJwtPayload
import com.paymentoptions.pos.utils.getDeviceTimeZone
import com.paymentoptions.pos.utils.getKeyFromToken
import kotlinx.datetime.Clock

/**
 * Unified coordinator for MineSec SoftPOS payments.
 * Handles the orchestration of Daspay API calls and Native SDK triggers.
 */
object MineSecSDK {

    /**
     * Orchestrates a Tap-to-Pay transaction.
     * 1. Prepares data and registers payment with Daspay Backend.
     * 2. Triggers the Platform-Specific Native SDK.
     */
    suspend fun processSale(
        amount: String,
        onNativeTrigger: suspend (posReference: String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val authDetails = DPStorageManager.getAuthDetails() ?: throw Exception("Session expired")
            val data = authDetails.data ?: throw Exception("Invalid session")
            
            val currency = DPStorageManager.getTransactionCurrency()
            val decodedJwt = decodeJwtPayload(data.token.idToken)
            val email = getKeyFromToken(decodedJwt, "email")
            val contact = getKeyFromToken(decodedJwt, "custom:ContactNo")

            // 1. Register the payment intention with Daspay Backend
            val paymentRequest = PaymentRequest(
                amount = amount,
                currency = currency,
                merchant_txn_ref = "KMP_${Clock.System.now().toEpochMilliseconds()}",
                customer_ip = getDeviceIpAddress(),
                merchant_id = DPStorageManager.getTapPayDasmid(),
                return_url = PaymentReturnUrl(
                    webhook_url = "https://webhook.site/placeholder",
                    success_url = "https://success.placeholder",
                    decline_url = "https://decline.placeholder"
                ),
                billing_address = Address(
                    country = "SG", email = email, address1 = "Mobile Device",
                    phone_number = contact, city = "Singapore", state = "SG", postal_code = "000000"
                ),
                shipping_address = Address(
                    country = "SG", email = email, address1 = "Mobile Device",
                    phone_number = contact, city = "Singapore", state = "SG", postal_code = "000000"
                ),
                payment_method = PaymentMethodRequest(type = "daspay"),
                time_zone = getDeviceTimeZone()
            )

            val response = payment(paymentRequest)
            if (response?.success == true) {
                // 2. Pass the reference to the native UI (Android Activity or iOS Swift Provider)
                onNativeTrigger(response.transaction_details.id)
            } else {
                onError("Failed to initialize payment with backend")
            }
        } catch (e: Exception) {
            onError(e.message ?: "An unexpected error occurred")
        }
    }
}

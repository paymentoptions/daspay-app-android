package com.paymentoptions.pos.payment

/**
 * Result returned from native MineSec SDK operations (sale, void, refund).
 */
data class MineSecTransactionResult(
    val success: Boolean,
    val tranId: String? = null,
    val posReference: String? = null,
    val acquirerResponseJson: String? = null,
    val tranType: String? = null,
    val errorMessage: String? = null,
)

/**
 * Interface to bridge Kotlin calls to the native MineSec iOS SDK.
 */
interface MineSecPaymentProvider {
    /**
     * Triggers a sale request.
     * @param onResult Callback with a single [MineSecTransactionResult] object.
     */
    fun launchSale(
        amount: String,
        currency: String,
        description: String?,
        posReference: String,
        onResult: (MineSecTransactionResult) -> Unit
    )

    fun launchVoid(
        acquirerTransactionId: String,
        profileId: String,
        onResult: (MineSecTransactionResult) -> Unit
    )

    fun launchLinkedRefund(
        acquirerTransactionId: String,
        amount: String,
        currency: String,
        profileId: String,
        onResult: (MineSecTransactionResult) -> Unit
    )
}

/**
 * Singleton to hold the provider instance injected from the iOS App.
 */
object MineSecPlatform {
    var paymentProvider: MineSecPaymentProvider? = null

    /** Profile ID used for MineSec POI requests — must match the activated SoftPOS profile. */
    const val PROFILE_ID = "prof_01KH8NQC4PVFKRNH31ZPC2QJNN"
}

package com.paymentoptions.pos.services.analytics

sealed class AnalyticsEvent {
    abstract val name: String
    open val attributes: Map<String, Any?> = emptyMap()

    data class ScreenViewed(val screenName: String) : AnalyticsEvent() {
        override val name: String = "screen_viewed"
        override val attributes: Map<String, Any?> = mapOf("screen_name" to screenName)
    }

    data class PaymentStarted(
        val paymentType: String,
        val amount: String,
        val currency: String
    ) : AnalyticsEvent() {
        override val name: String = "payment_started"
        override val attributes: Map<String, Any?> = mapOf(
            "payment_type" to paymentType,
            "amount" to amount,
            "currency" to currency
        )
    }

    data class PaymentCompleted(
        val paymentType: String,
        val transactionId: String,
        val status: String
    ) : AnalyticsEvent() {
        override val name: String = "payment_completed"
        override val attributes: Map<String, Any?> = mapOf(
            "payment_type" to paymentType,
            "transaction_id" to transactionId,
            "status" to status
        )
    }

    data class Custom(
        override val name: String,
        override val attributes: Map<String, Any?> = emptyMap()
    ) : AnalyticsEvent()
}


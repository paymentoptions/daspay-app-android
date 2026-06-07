package com.paymentoptions.pos.services.analytics

object AppAnalytics {
    private fun event(name: String, attributes: Map<String, Any?> = emptyMap()) {
        AnalyticsHelper.track(AnalyticsEvent.Custom(name = name, attributes = attributes))
    }

    fun appLaunch(variant: String) = event(
        name = "app_launch",
        attributes = mapOf("variant" to variant)
    )

    fun login(result: String, email: String? = null) = event(
        name = "login_$result",
        attributes = mapOf("email" to email)
    )

    fun logout(result: String, source: String) = event(
        name = "logout_$result",
        attributes = mapOf("source" to source)
    )

    fun merchantSelection(paymentType: String, dasmid: String) = event(
        name = "merchant_selection",
        attributes = mapOf(
            "payment_type" to paymentType,
            "dasmid" to dasmid
        )
    )

    fun dashboardNavigation(destination: String, source: String) = event(
        name = "dashboard_navigation",
        attributes = mapOf(
            "destination" to destination,
            "source" to source
        )
    )

    fun tapToPayInitiated(amount: String, currency: String) = event(
        name = "tap_to_pay_initiated",
        attributes = mapOf("amount" to amount, "currency" to currency)
    )

    fun mineSecSdkInitialization(step: String, result: String, details: String? = null) = event(
        name = "minesec_sdk_initialization",
        attributes = mapOf(
            "step" to step,
            "result" to result,
            "details" to details
        )
    )

    fun profileDownloadOrActivation(profileId: String, stage: String, result: String) = event(
        name = "profile_download_activation",
        attributes = mapOf(
            "profile_id" to profileId,
            "stage" to stage,
            "result" to result
        )
    )

    fun qrPaymentInitiated(amount: String, currency: String) = event(
        name = "qr_payment_initiated",
        attributes = mapOf("amount" to amount, "currency" to currency)
    )

    fun payByLinkCreated(productId: String?, amount: Any?, currency: String) = event(
        name = "pay_by_link_created",
        attributes = mapOf(
            "product_id" to productId,
            "amount" to amount,
            "currency" to currency
        )
    )

    fun settlementSubmitted(batchId: String) = event(
        name = "settlement_submitted",
        attributes = mapOf("batch_id" to batchId)
    )

    fun settlementResult(batchId: String, result: String) = event(
        name = "settlement_$result",
        attributes = mapOf("batch_id" to batchId)
    )

    fun apiError(endpoint: String, method: String, code: Int?, message: String? = null) {
        AnalyticsHelper.trackError(
            message = "api_error",
            attributes = mapOf(
                "endpoint" to endpoint,
                "method" to method,
                "status_code" to code,
                "error_message" to message
            )
        )
    }

    fun screenNavigation(route: String, previousRoute: String?) = event(
        name = "screen_navigation",
        attributes = mapOf(
            "route" to route,
            "previous_route" to previousRoute
        )
    )

    fun criticalButtonClick(buttonName: String, screen: String) = event(
        name = "critical_button_click",
        attributes = mapOf(
            "button_name" to buttonName,
            "screen" to screen
        )
    )

    fun appCrash(throwable: Throwable, threadName: String?) {
        AnalyticsHelper.trackError(
            message = "app_crash_exception",
            throwable = throwable,
            attributes = mapOf(
                "thread_name" to threadName,
                "exception_type" to throwable::class.java.simpleName
            )
        )
    }

    fun paymentStatus(status: String, paymentType: String, transactionId: String?) = event(
        name = when {
            status.contains("APPROV", ignoreCase = true) -> "payment_approved"
            status.contains("DECLIN", ignoreCase = true) -> "payment_declined"
            status.contains("CANCEL", ignoreCase = true) -> "payment_cancelled"
            status.contains("FAIL", ignoreCase = true) -> "payment_failed"
            else -> "payment_status"
        },
        attributes = mapOf(
            "status" to status,
            "payment_type" to paymentType,
            "transaction_id" to transactionId
        )
    )
}


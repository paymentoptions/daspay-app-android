package com.paymentoptions.pos.analytics

import com.datadog.android.rum.GlobalRumMonitor
import com.datadog.android.rum.Rum
import com.datadog.android.rum.RumActionType
import com.datadog.android.rum.RumErrorSource
import com.paymentoptions.pos.platformLog
import com.paymentoptions.pos.platformLogError
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Datadog-backed [AnalyticsDelegate]. Register at app startup via
 * `AnalyticsHelper.delegate = DatadogAnalyticsDelegate`.
 */
object DatadogAnalyticsDelegate : AnalyticsDelegate {

    private const val KEY_ENVIRONMENT = "environment"
    private const val KEY_VARIANT = "variant"
    private const val KEY_STATUS = "status"
    private const val KEY_FROM = "from"
    private const val KEY_TO = "to"
    private const val KEY_SCREEN = "screen"
    private const val KEY_ACTION = "action"

    private val crashHandlerInstalled = AtomicBoolean(false)
    
    var environment: String = ""
    var variant: String = ""

    override fun trackAppLaunch() {
        track(AnalyticsEvent.APP_LAUNCH, emptyMap())
    }

    override fun trackLogin(userId: String?, merchantId: String?) {
        track(
            event = AnalyticsEvent.LOGIN,
            attributes = mapOfNotNull(
                "user_id" to userId,
                "merchant_id" to merchantId,
            ),
        )
    }

    override fun trackLogout(userId: String?) {
        track(
            event = AnalyticsEvent.LOGOUT,
            attributes = mapOfNotNull("user_id" to userId),
        )
    }

    override fun trackMerchantSelection(merchantId: String, merchantName: String?) {
        track(
            event = AnalyticsEvent.MERCHANT_SELECTION,
            attributes = mapOfNotNull(
                "merchant_id" to merchantId,
                "merchant_name" to merchantName,
            ),
        )
    }

    override fun trackDashboardNavigation(section: String) {
        track(
            event = AnalyticsEvent.DASHBOARD_NAVIGATION,
            attributes = mapOf(KEY_ACTION to section),
        )
    }

    override fun trackTapToPayInitiated(amount: String?, currency: String?) {
        track(
            event = AnalyticsEvent.TAP_TO_PAY_INITIATED,
            attributes = mapOfNotNull(
                "amount" to amount,
                "currency" to currency,
            ),
        )
    }

    override fun trackMineSecSdkInitialization(success: Boolean, reason: String?) {
        track(
            event = AnalyticsEvent.MINESEC_SDK_INITIALIZATION,
            attributes = mapOfNotNull(
                KEY_STATUS to successStatus(success),
                "reason" to reason,
            ),
        )
    }

    override fun trackProfileDownload(success: Boolean, reason: String?) {
        track(
            event = AnalyticsEvent.PROFILE_DOWNLOAD,
            attributes = mapOfNotNull(
                KEY_STATUS to successStatus(success),
                "reason" to reason,
            ),
        )
    }

    override fun trackProfileActivation(success: Boolean, reason: String?) {
        track(
            event = AnalyticsEvent.PROFILE_ACTIVATION,
            attributes = mapOfNotNull(
                KEY_STATUS to successStatus(success),
                "reason" to reason,
            ),
        )
    }

    override fun trackPaymentStarted(paymentMethod: String, amount: String?, currency: String?) {
        track(
            event = AnalyticsEvent.PAYMENT_STARTED,
            attributes = mapOfNotNull(
                "payment_method" to paymentMethod,
                "amount" to amount,
                "currency" to currency,
            ),
        )
    }

    override fun trackPaymentApproved(transactionId: String?, amount: String?) {
        track(
            event = AnalyticsEvent.PAYMENT_APPROVED,
            attributes = mapOfNotNull(
                "transaction_id" to transactionId,
                "amount" to amount,
            ),
        )
    }

    override fun trackPaymentDeclined(reason: String?, transactionId: String?) {
        track(
            event = AnalyticsEvent.PAYMENT_DECLINED,
            attributes = mapOfNotNull(
                "reason" to reason,
                "transaction_id" to transactionId,
            ),
        )
    }

    override fun trackPaymentFailed(reason: String?, transactionId: String?, throwable: Throwable?) {
        track(
            event = AnalyticsEvent.PAYMENT_FAILED,
            attributes = mapOfNotNull(
                "reason" to reason,
                "transaction_id" to transactionId,
            ),
        )
        if (throwable != null) {
            trackException(
                event = AnalyticsEvent.PAYMENT_FAILED,
                throwable = throwable,
                attributes = mapOfNotNull("transaction_id" to transactionId),
            )
        }
    }

    override fun trackPaymentCancelled(reason: String?, transactionId: String?) {
        track(
            event = AnalyticsEvent.PAYMENT_CANCELLED,
            attributes = mapOfNotNull(
                "reason" to reason,
                "transaction_id" to transactionId,
            ),
        )
    }

    override fun trackQrPaymentInitiated(amount: String?, currency: String?) {
        track(
            event = AnalyticsEvent.QR_PAYMENT_INITIATED,
            attributes = mapOfNotNull(
                "amount" to amount,
                "currency" to currency,
            ),
        )
    }

    override fun trackPayByLinkCreated(linkId: String?, amount: String?) {
        track(
            event = AnalyticsEvent.PAY_BY_LINK_CREATED,
            attributes = mapOfNotNull(
                "link_id" to linkId,
                "amount" to amount,
            ),
        )
    }

    override fun trackSettlementSubmitted(batchId: String?) {
        track(
            event = AnalyticsEvent.SETTLEMENT_SUBMITTED,
            attributes = mapOfNotNull("batch_id" to batchId),
        )
    }

    override fun trackSettlementResult(success: Boolean, batchId: String?, reason: String?) {
        val event = if (success) AnalyticsEvent.SETTLEMENT_SUCCESSFUL else AnalyticsEvent.SETTLEMENT_FAILED
        track(
            event = event,
            attributes = mapOfNotNull(
                KEY_STATUS to successStatus(success),
                "batch_id" to batchId,
                "reason" to reason,
            ),
        )
    }

    override fun trackApiError(
        endpoint: String,
        statusCode: Int?,
        message: String,
        throwable: Throwable?,
    ) {
        val attributes = mapOfNotNull(
            "endpoint" to endpoint,
            "status_code" to statusCode,
        )
        track(event = AnalyticsEvent.API_ERROR, attributes = attributes + mapOf("message" to message))
        GlobalRumMonitor.get().addError(
            "${AnalyticsEvent.API_ERROR.eventName}: $message",
            RumErrorSource.NETWORK,
            throwable,
            withCommonAttributes(attributes),
        )
        platformLogError("Analytics", "API error tracked: endpoint=$endpoint, message=$message", throwable)
    }

    override fun trackScreenNavigation(fromScreen: String, toScreen: String) {
        trackInternal(
            event = AnalyticsEvent.SCREEN_NAVIGATION,
            attributes = mapOf(KEY_FROM to fromScreen, KEY_TO to toScreen),
            actionType = RumActionType.CUSTOM,
        )
    }

    override fun trackCriticalButtonClick(buttonName: String, screenName: String) {
        trackInternal(
            event = AnalyticsEvent.CRITICAL_BUTTON_CLICK,
            attributes = mapOf("button_name" to buttonName, KEY_SCREEN to screenName),
            actionType = RumActionType.TAP,
        )
    }

    override fun trackException(
        event: AnalyticsEvent,
        throwable: Throwable,
        attributes: Map<String, Any?>,
    ) {
        val payload = withCommonAttributes(attributes)
        GlobalRumMonitor.get().addError(event.eventName, RumErrorSource.SOURCE, throwable, payload)
        platformLogError("Analytics", "Exception tracked: ${event.eventName}", throwable)
    }

    override fun installGlobalCrashTracking() {
        if (!crashHandlerInstalled.compareAndSet(false, true)) return

        val existingHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            trackException(
                event = AnalyticsEvent.APP_CRASH,
                throwable = throwable,
                attributes = mapOf(
                    "thread_name" to thread.name,
                    "thread_hash" to thread.hashCode(),
                ),
            )
            existingHandler?.uncaughtException(thread, throwable)
        }
    }

    override fun track(event: AnalyticsEvent, attributes: Map<String, Any?>) {
        trackInternal(event = event, attributes = attributes, actionType = RumActionType.CUSTOM)
    }

    private fun trackInternal(
        event: AnalyticsEvent,
        attributes: Map<String, Any?>,
        actionType: RumActionType,
    ) {
        val payload = withCommonAttributes(attributes)
        GlobalRumMonitor.get().addAction(actionType, event.eventName, payload)
        platformLog("Analytics", "Event tracked: ${event.eventName}")
    }

    private fun withCommonAttributes(attributes: Map<String, Any?>): Map<String, Any?> {
        return mapOf(
            KEY_ENVIRONMENT to environment,
            KEY_VARIANT to variant,
        ) + attributes
    }

    private fun successStatus(success: Boolean): String {
        return if (success) "success" else "failed"
    }

    private fun <K, V> mapOfNotNull(vararg pairs: Pair<K, V?>): Map<K, V> {
        val result = LinkedHashMap<K, V>(pairs.size)
        for ((key, value) in pairs) {
            if (value != null) result[key] = value
        }
        return result
    }
}

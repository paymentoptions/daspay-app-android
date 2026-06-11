package com.paymentoptions.pos.analytics

import kotlin.native.concurrent.ThreadLocal

/**
 * Cross-platform analytics façade. Delegates to an [AnalyticsDelegate]
 * registered at app startup.
 */
@ThreadLocal
object AnalyticsHelper {

    var delegate: AnalyticsDelegate = NoOpAnalyticsDelegate

    fun trackAppLaunch() = delegate.trackAppLaunch()

    fun trackLogin(userId: String? = null, merchantId: String? = null) =
        delegate.trackLogin(userId, merchantId)

    fun trackLogout(userId: String? = null) =
        delegate.trackLogout(userId)

    fun trackMerchantSelection(merchantId: String, merchantName: String? = null) =
        delegate.trackMerchantSelection(merchantId, merchantName)

    fun trackDashboardNavigation(section: String) =
        delegate.trackDashboardNavigation(section)

    fun trackTapToPayInitiated(amount: String? = null, currency: String? = null) =
        delegate.trackTapToPayInitiated(amount, currency)

    fun trackMineSecSdkInitialization(success: Boolean, reason: String? = null) =
        delegate.trackMineSecSdkInitialization(success, reason)

    fun trackProfileDownload(success: Boolean, reason: String? = null) =
        delegate.trackProfileDownload(success, reason)

    fun trackProfileActivation(success: Boolean, reason: String? = null) =
        delegate.trackProfileActivation(success, reason)

    fun trackPaymentStarted(paymentMethod: String, amount: String? = null, currency: String? = null) =
        delegate.trackPaymentStarted(paymentMethod, amount, currency)

    fun trackPaymentApproved(transactionId: String? = null, amount: String? = null) =
        delegate.trackPaymentApproved(transactionId, amount)

    fun trackPaymentDeclined(reason: String? = null, transactionId: String? = null) =
        delegate.trackPaymentDeclined(reason, transactionId)

    fun trackPaymentFailed(reason: String? = null, transactionId: String? = null, throwable: Throwable? = null) =
        delegate.trackPaymentFailed(reason, transactionId, throwable)

    fun trackPaymentCancelled(reason: String? = null, transactionId: String? = null) =
        delegate.trackPaymentCancelled(reason, transactionId)

    fun trackQrPaymentInitiated(amount: String? = null, currency: String? = null) =
        delegate.trackQrPaymentInitiated(amount, currency)

    fun trackPayByLinkCreated(linkId: String? = null, amount: String? = null) =
        delegate.trackPayByLinkCreated(linkId, amount)

    fun trackSettlementSubmitted(batchId: String? = null) =
        delegate.trackSettlementSubmitted(batchId)

    fun trackSettlementResult(success: Boolean, batchId: String? = null, reason: String? = null) =
        delegate.trackSettlementResult(success, batchId, reason)

    fun trackApiError(endpoint: String, statusCode: Int? = null, message: String, throwable: Throwable? = null) =
        delegate.trackApiError(endpoint, statusCode, message, throwable)

    fun trackScreenNavigation(fromScreen: String, toScreen: String) =
        delegate.trackScreenNavigation(fromScreen, toScreen)

    fun trackCriticalButtonClick(buttonName: String, screenName: String) =
        delegate.trackCriticalButtonClick(buttonName, screenName)

    fun trackException(
        event: AnalyticsEvent = AnalyticsEvent.APP_EXCEPTION,
        throwable: Throwable,
        attributes: Map<String, Any?> = emptyMap()
    ) = delegate.trackException(event, throwable, attributes)

    fun installGlobalCrashTracking() = delegate.installGlobalCrashTracking()

    fun track(event: AnalyticsEvent, attributes: Map<String, Any?> = emptyMap()) =
        delegate.track(event, attributes)
}

package com.paymentoptions.pos.analytics

interface AnalyticsDelegate {
    fun trackAppLaunch()
    fun trackLogin(userId: String?, merchantId: String?)
    fun trackLogout(userId: String?)
    fun trackMerchantSelection(merchantId: String, merchantName: String?)
    fun trackDashboardNavigation(section: String)
    fun trackTapToPayInitiated(amount: String?, currency: String?)
    fun trackMineSecSdkInitialization(success: Boolean, reason: String?)
    fun trackProfileDownload(success: Boolean, reason: String?)
    fun trackProfileActivation(success: Boolean, reason: String?)
    fun trackPaymentStarted(paymentMethod: String, amount: String?, currency: String?)
    fun trackPaymentApproved(transactionId: String?, amount: String?)
    fun trackPaymentDeclined(reason: String?, transactionId: String?)
    fun trackPaymentFailed(reason: String?, transactionId: String?, throwable: Throwable?)
    fun trackPaymentCancelled(reason: String?, transactionId: String?)
    fun trackQrPaymentInitiated(amount: String?, currency: String?)
    fun trackPayByLinkCreated(linkId: String?, amount: String?)
    fun trackSettlementSubmitted(batchId: String?)
    fun trackSettlementResult(success: Boolean, batchId: String?, reason: String?)
    fun trackApiError(endpoint: String, statusCode: Int?, message: String, throwable: Throwable?)
    fun trackScreenNavigation(fromScreen: String, toScreen: String)
    fun trackCriticalButtonClick(buttonName: String, screenName: String)
    fun trackException(event: AnalyticsEvent, throwable: Throwable, attributes: Map<String, Any?>)
    fun installGlobalCrashTracking()
    fun track(event: AnalyticsEvent, attributes: Map<String, Any?>)
}

object NoOpAnalyticsDelegate : AnalyticsDelegate {
    override fun trackAppLaunch() {}
    override fun trackLogin(userId: String?, merchantId: String?) {}
    override fun trackLogout(userId: String?) {}
    override fun trackMerchantSelection(merchantId: String, merchantName: String?) {}
    override fun trackDashboardNavigation(section: String) {}
    override fun trackTapToPayInitiated(amount: String?, currency: String?) {}
    override fun trackMineSecSdkInitialization(success: Boolean, reason: String?) {}
    override fun trackProfileDownload(success: Boolean, reason: String?) {}
    override fun trackProfileActivation(success: Boolean, reason: String?) {}
    override fun trackPaymentStarted(paymentMethod: String, amount: String?, currency: String?) {}
    override fun trackPaymentApproved(transactionId: String?, amount: String?) {}
    override fun trackPaymentDeclined(reason: String?, transactionId: String?) {}
    override fun trackPaymentFailed(reason: String?, transactionId: String?, throwable: Throwable?) {}
    override fun trackPaymentCancelled(reason: String?, transactionId: String?) {}
    override fun trackQrPaymentInitiated(amount: String?, currency: String?) {}
    override fun trackPayByLinkCreated(linkId: String?, amount: String?) {}
    override fun trackSettlementSubmitted(batchId: String?) {}
    override fun trackSettlementResult(success: Boolean, batchId: String?, reason: String?) {}
    override fun trackApiError(endpoint: String, statusCode: Int?, message: String, throwable: Throwable?) {}
    override fun trackScreenNavigation(fromScreen: String, toScreen: String) {}
    override fun trackCriticalButtonClick(buttonName: String, screenName: String) {}
    override fun trackException(event: AnalyticsEvent, throwable: Throwable, attributes: Map<String, Any?>) {}
    override fun installGlobalCrashTracking() {}
    override fun track(event: AnalyticsEvent, attributes: Map<String, Any?>) {}
}

package com.paymentoptions.pos.analytics

/**
 * Canonical event names used for analytics/RUM action/error tracking.
 */
enum class AnalyticsEvent(val eventName: String) {
    APP_LAUNCH("App Launch"),
    LOGIN("Login"),
    LOGOUT("Logout"),
    MERCHANT_SELECTION("Merchant Selection"),
    DASHBOARD_NAVIGATION("Dashboard Navigation"),
    TAP_TO_PAY_INITIATED("Tap to Pay Initiated"),
    MINESEC_SDK_INITIALIZATION("MineSec SDK Initialization"),
    PROFILE_DOWNLOAD("Profile Download"),
    PROFILE_ACTIVATION("Profile Activation"),
    PAYMENT_STARTED("Payment Started"),
    PAYMENT_APPROVED("Payment Approved"),
    PAYMENT_DECLINED("Payment Declined"),
    PAYMENT_FAILED("Payment Failed"),
    PAYMENT_CANCELLED("Payment Cancelled"),
    QR_PAYMENT_INITIATED("QR Payment Initiated"),
    PAY_BY_LINK_CREATED("Pay By Link Created"),
    SETTLEMENT_SUBMITTED("Settlement Submitted"),
    SETTLEMENT_SUCCESSFUL("Settlement Successful"),
    SETTLEMENT_FAILED("Settlement Failed"),
    API_ERROR("API Errors"),
    SCREEN_NAVIGATION("Screen Navigation Events"),
    CRITICAL_BUTTON_CLICK("Button Clicks on Critical Actions"),
    APP_CRASH("App Crash"),
    APP_EXCEPTION("App Exception"),
}

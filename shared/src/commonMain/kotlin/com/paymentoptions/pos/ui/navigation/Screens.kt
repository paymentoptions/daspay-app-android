package com.paymentoptions.pos.ui.navigation

import com.paymentoptions.pos.urlEncode

sealed class Screens(val route: String) {

    // Biometrics & Auth Check
    object FingerprintScan : Screens("FingerprintScan")
    object AuthCheck       : Screens("AuthCheck")

    // Auth
    object SignIn : Screens("SignIn")
    object Token  : Screens("Token")

    // Bottom Navigation
    object Dashboard      : Screens("Dashboard")
    object FoodOrderFlow  : Screens("FoodOrderFlow")
    object ReceiveMoneyFlow : Screens("ReceiveMoneyFlow")
    object Notifications  : Screens("Notifications")

    // Transaction flows
    object TransactionHistory : Screens("TransactionHistory")
    object TransactionReceipt : Screens("TransactionRecept")   // typo kept for route compatibility

    object TransactionAction : Screens("TransactionAction/{transactionJson}/{action}") {
        fun createRoute(transactionJson: String, action: String) =
            "TransactionAction/${urlEncode(transactionJson)}/$action"
    }

    object TransactionDetails : Screens("TransactionDetails/{transactionJson}") {
        fun createRoute(transactionJson: String) =
            "TransactionDetails/${urlEncode(transactionJson)}"
    }

    // Menus
    object QueryScreen    : Screens("Query")
    object RefundInitiated: Screens("RefundInitiated")
    object Settings       : Screens("Settings")
    object HelpAndSupport : Screens("HelpAndSupport")
    object SendLogs       : Screens("SendLogs")

    // Settlement
    object Settlement     : Screens("Settlement")
    object SettlementAction : Screens("SettlementAction/{settleId}") {
        fun createRoute(settleId: String) = "SettlementAction/${urlEncode(settleId)}"
    }

    // Misc
    object Splash    : Screens("Splash")
    object FcmToken  : Screens("FcmToken")
    object Status    : Screens("Status")
}

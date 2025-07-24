package com.paymentoptions.pos.ui.composables.navigation

sealed class Screens(val route: String) {
    //Biometrics & Auth Check------------------------------------------
    object FingerprintScan : Screens(route = "FingerprintScan")

    //    object FaceRecognition : Screens(route = "FaceRecognition")
    object AuthCheck : Screens(route = "AuthCheck")
    //-----------------------------------------------------------------

    //Auth ------------------------------------------------------------
    object SignIn : Screens(route = "SignIn")
    object Token : Screens(route = "Token")
    //-----------------------------------------------------------------

    //Bottom Navigation -----------------------------------------------
    object Dashboard : Screens(route = "Dashboard")
    object FoodOrderFlow : Screens(route = "FoodOrderFlow")
    object ReceiveMoneyFlow : Screens(route = "ReceiveMoneyFlow")
    object Notifications : Screens(route = "Notifications")
    //-----------------------------------------------------------------

    // More Menu Items ------------------------------------------------
    object TransactionHistory : Screens(route = "TransactionHistory")

    //------------------------------------------------------------------
    object Refund : Screens(route = "Refund")
    object RefundTransaction : Screens(route = "RefundTransaction")
    object RefundInitiated : Screens(route = "RefundInitiated")
    //------------------------------------------------------------------
    object Settings : Screens(route = "Settings")
    object HelpAndSupport : Screens(route = "HelpAndSupport")

    //Misc
    object Splash : Screens(route = "Splash")
    object FcmToken : Screens(route = "FcmToken")
    object Status : Screens(route = "Status")
}
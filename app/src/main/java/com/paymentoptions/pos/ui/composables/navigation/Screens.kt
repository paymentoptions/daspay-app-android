package com.paymentoptions.pos.ui.composables.navigation

sealed class Screens(val route: String) {
    //Biometrics & Auth Check------------------------------------------
    object FingerprintScan : Screens(route = "FingerprintScan")
    object FaceRecognition : Screens(route = "FaceRecognition")
    object AuthCheck : Screens(route = "AuthCheck")
    //-----------------------------------------------------------------

    //Auth ------------------------------------------------------------
    object SignIn : Screens(route = "SignIn")
    object Token : Screens(route = "Token")
    //-----------------------------------------------------------------

    //Bottom Navigation -----------------------------------------------
    object Dashboard : Screens(route = "Dashboard")
    object FoodOrder : Screens(route = "FoodOrder")
    object ReceiveMoney : Screens(route = "ReceiveMoney")
    object Notifications : Screens(route = "Notifications")
    //-----------------------------------------------------------------

    object TransactionHistory : Screens(route = "TransactionHistory")

    //Refund Flow-------------------------------------------------------
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
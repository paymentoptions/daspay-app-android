package com.paymentoptions.pos.ui.composables.navigation


sealed class Screens(val route: String) {
    //Biometrics & Auth Check
    object FingerprintScan : Screens(route = "FingerprintScan")
    object FaceRecognition : Screens(route = "FaceRecognition")
    object AuthCheck : Screens(route = "AuthCheck")

    //Auth
    object SignIn : Screens(route = "SignIn")
    object Token : Screens(route = "Token")

    //Bottom Navigation
    object Dashboard : Screens(route = "Dashboard")
    object FoodMenu : Screens(route = "FoodMenu")
    object ReceiveMoney : Screens(route = "ReceiveMoney")
    object Notifications : Screens(route = "Notifications")

    //Bottom Navigation Hamburger Menu
    object SendMoney : Screens(route = "SendMoney")
    object TransactionHistory : Screens(route = "TransactionHistory")
    object Settlement : Screens(route = "Settlement")
    object Refund : Screens(route = "Refund")
    object TotalSales : Screens(route = "TotalSales")
    object Settings : Screens(route = "Settings")
    object HelpAndSupport : Screens(route = "HelpAndSupport")
    object AppVersion : Screens(route = "AppVersion")

    //Misc
    object SplashScreen : Screens(route = "Splash")
    object FcmTokenScreen : Screens(route = "FcmToken")
}
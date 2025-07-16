package com.paymentoptions.pos.ui.composables.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paymentoptions.pos.ui.composables.screens._test.fcmtoken.FcmTokenScreen
import com.paymentoptions.pos.ui.composables.screens.authcheck.AuthCheckScreen
import com.paymentoptions.pos.ui.composables.screens.dashboard.DashboardScreen
import com.paymentoptions.pos.ui.composables.screens.fingerprintscan.FingerprintScanScreen
import com.paymentoptions.pos.ui.composables.screens.foodmenu.FoodMenuScreen
import com.paymentoptions.pos.ui.composables.screens.helpandsupport.HelpAndSupportScreen
import com.paymentoptions.pos.ui.composables.screens.notifications.NotificationsScreen
import com.paymentoptions.pos.ui.composables.screens.receivemoney.ReceiveMoneyScreen
import com.paymentoptions.pos.ui.composables.screens.refund.RefundScreen
import com.paymentoptions.pos.ui.composables.screens.refundTransaction.RefundTransactionScreen
import com.paymentoptions.pos.ui.composables.screens.refundinitiated.RefundInitiatedScreen
import com.paymentoptions.pos.ui.composables.screens.settings.SettingsScreen
import com.paymentoptions.pos.ui.composables.screens.signIn.SignInScreen
import com.paymentoptions.pos.ui.composables.screens.splash.SplashScreen
import com.paymentoptions.pos.ui.composables.screens.token.TokenScreen
import com.paymentoptions.pos.ui.composables.screens.transactionshistory.TransactionHistoryScreen


@Composable
fun Navigator() {
    val navController = rememberNavController()
    val startDestination = Screens.Splash.route

//    LaunchedEffect(navController) {
//        navController.currentBackStackEntryFlow.collect { backStackEntry ->
//            navController.currentBackStackEntryFlow.collect { backStackEntry ->
//            }
//        }
//    }

    NavHost(navController = navController, startDestination = startDestination) {
        //Biometrics & Auth Check
        composable(Screens.FingerprintScan.route) { FingerprintScanScreen(navController, {}, {}) }
        //composable(Screens.FaceRecognition.route) { (navController) }
        composable(Screens.AuthCheck.route) { AuthCheckScreen(navController) }

        //Auth
        composable(Screens.SignIn.route) { SignInScreen(navController) }
        composable(Screens.Token.route) { TokenScreen(navController) }

        //Bottom Navigation
        composable(Screens.Dashboard.route) { DashboardScreen(navController) }

        composable(Screens.FoodMenu.route) { FoodMenuScreen(navController) }
//        composable(Screens.ReviewOrder.route) { ReviewOrderScreen(navController) }
//        composable(Screens.AdditionalCharge.route) { AdditionalChargeScreen(navController) }

        composable(Screens.ReceiveMoney.route) { ReceiveMoneyScreen(navController) }
        composable(Screens.Notifications.route) { NotificationsScreen(navController) }

        composable(Screens.TransactionHistory.route) { TransactionHistoryScreen(navController) }

        composable(Screens.Refund.route) { RefundScreen(navController) }
        composable(Screens.RefundTransaction.route) { RefundTransactionScreen(navController) }
        composable(Screens.RefundInitiated.route) { RefundInitiatedScreen(navController) }

        composable(Screens.Settings.route) { SettingsScreen(navController) }
        composable(Screens.HelpAndSupport.route) { HelpAndSupportScreen(navController) }

        //Misc
        composable(Screens.Splash.route) { SplashScreen(navController) }
        composable(Screens.FcmToken.route) { FcmTokenScreen(navController) }
//        composable(Screens.Status.route) { StatusScreen(navController) }
    }
}

//
//@Composable
//fun NavController.navigateSecurely(route: String, fallback: String? = null) {
//    val context = LocalContext.current
//
//    try {
//        this.navigate(route)
//    } catch (e: Exception) {
//        Toast.makeText(context, "Unable to navigate to $route", Toast.LENGTH_SHORT).show()
//    } finally {
//
//    }
//}

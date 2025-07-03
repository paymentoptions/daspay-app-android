package com.paymentoptions.pos.ui.composables.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paymentoptions.pos.ui.composables.screens.authcheckscreen.AuthCheckScreen
import com.paymentoptions.pos.ui.composables.screens.dashboard.DashboardScreen
import com.paymentoptions.pos.ui.composables.screens.fingerprintscan.FingerprintScanScreen
import com.paymentoptions.pos.ui.composables.screens.foodmenu.FoodMenuScreen
import com.paymentoptions.pos.ui.composables.screens.helpandsupport.HelpAndSupportScreen
import com.paymentoptions.pos.ui.composables.screens.loading.LoadingScreen
import com.paymentoptions.pos.ui.composables.screens.notifications.NotificationsScreen
import com.paymentoptions.pos.ui.composables.screens.receivemoney.ReceiveMoneyScreen
import com.paymentoptions.pos.ui.composables.screens.refund.RefundScreen
import com.paymentoptions.pos.ui.composables.screens.settings.SettingsScreen
import com.paymentoptions.pos.ui.composables.screens.signIn.SignInScreen
import com.paymentoptions.pos.ui.composables.screens.signout.SignOutScreen
import com.paymentoptions.pos.ui.composables.screens.token.TokenScreen
import com.paymentoptions.pos.ui.composables.screens.transactionshistory.TransactionHistoryScreen


@Composable
fun Navigator() {
    val navController = rememberNavController()
    val startDestination = Screens.TransactionHistory.route

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
        composable(Screens.SignOut.route) { SignOutScreen(navController) }
        composable(Screens.Token.route) { TokenScreen(navController) }

        //Bottom Navigation
        composable(Screens.Dashboard.route) { DashboardScreen(navController) }
        composable(Screens.FoodMenu.route) { FoodMenuScreen(navController) }
        composable(Screens.ReceiveMoney.route) { ReceiveMoneyScreen(navController) }
        composable(Screens.Notifications.route) { NotificationsScreen(navController) }


        //Bottom Navigation Hamburger Menu
        //composable(Screens.SendMoney.route) { SendMoneyScreen(navController) }
        composable(Screens.TransactionHistory.route) { TransactionHistoryScreen(navController) }
        //composable(Screens.Settlement.route) { SettlementScreen(navController) }
        composable(Screens.Refund.route) { RefundScreen(navController) }

        //composable(Screens.TotalSales.route) { TotalSalesScreen(navController) }
        composable(Screens.Settings.route) { SettingsScreen(navController) }
        composable(Screens.HelpAndSupport.route) { HelpAndSupportScreen(navController) }
        //composable(Screens.AppVersion.route) { AppVersionScreen(navController) }


        //Misc
        composable(Screens.Loading.route) { LoadingScreen(navController) }
    }
}
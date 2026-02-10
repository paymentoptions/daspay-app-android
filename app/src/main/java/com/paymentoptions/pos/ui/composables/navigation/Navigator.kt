package com.paymentoptions.pos.ui.composables.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.paymentoptions.pos.services.apiService.AuthEventManager
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.FoodOrderFlow
import com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow.ReceiveMoneyFlow
import com.paymentoptions.pos.ui.composables.screens._flow.refundFlow.refund.RefundScreen
import com.paymentoptions.pos.ui.composables.screens._flow.refundFlow.refundTransaction.RefundTransactionScreen
import com.paymentoptions.pos.ui.composables.screens._flow.refundFlow.refundinitiated.RefundInitiatedScreen
import com.paymentoptions.pos.ui.composables.screens._test.fcmtoken.FcmTokenScreen
import com.paymentoptions.pos.ui.composables.screens.authcheck.AuthCheckScreen
import com.paymentoptions.pos.ui.composables.screens.dashboard.DashboardScreen
import com.paymentoptions.pos.ui.composables.screens.fingerprintscan.FingerprintAutoLoginScreen
import com.paymentoptions.pos.ui.composables.screens.fingerprintscan.FingerprintScanScreen
import com.paymentoptions.pos.ui.composables.screens.helpandsupport.HelpAndSupportScreen
import com.paymentoptions.pos.ui.composables.screens.notifications.NotificationsScreen
import com.paymentoptions.pos.ui.composables.screens.settings.SettingsScreen
import com.paymentoptions.pos.ui.composables.screens.settlement.SettlementScreen
import com.paymentoptions.pos.ui.composables.screens.signIn.SignInScreen
import com.paymentoptions.pos.ui.composables.screens.splash.SplashScreen
import com.paymentoptions.pos.ui.composables.screens.token.TokenScreen
import com.paymentoptions.pos.ui.composables.screens.transactiondetails.TransactionDetailsScreen
import com.paymentoptions.pos.ui.composables.screens.transactionshistory.TransactionHistoryScreen
import kotlinx.coroutines.flow.collectLatest
import java.net.URLDecoder


@Composable
fun Navigator() {
    val navController = rememberNavController()
    val startDestination = Screens.Splash.route

    // Observe auth events for global navigation handling
    LaunchedEffect(Unit) {
        AuthEventManager.authEvents.collectLatest { event ->
            when (event) {
                is AuthEventManager.AuthEvent.RequireReAuthentication -> {
                    // Navigate to fingerprint scan for auto sign-in
                    println("Navigator: Received RequireReAuthentication event, navigating to FingerprintScan")
                    navController.navigate(Screens.FingerprintScan.route) {
                        // Clear back stack to prevent going back to authenticated screens
                        popUpTo(0) { inclusive = true }
                    }
                }
                is AuthEventManager.AuthEvent.RequireManualSignIn -> {
                    // Navigate to sign-in screen for manual authentication
                    println("Navigator: Received RequireManualSignIn event, navigating to SignIn")
                    navController.navigate(Screens.SignIn.route) {
                        // Clear back stack to prevent going back to authenticated screens
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        //Biometrics & Auth Check
        composable(Screens.FingerprintScan.route) { FingerprintAutoLoginScreen(navController, ) }
//        composable(Screens.FaceRecognition.route) { (navController) }
        composable(Screens.AuthCheck.route) { AuthCheckScreen(navController) }

        //Auth
        composable(Screens.SignIn.route) { SignInScreen(navController) }
        composable(Screens.Token.route) { TokenScreen(navController) }

        //Bottom Navigation
        composable(Screens.Dashboard.route) { DashboardScreen(navController) }
        composable(Screens.FoodOrderFlow.route) { FoodOrderFlow(navController) }
        composable(Screens.ReceiveMoneyFlow.route) { ReceiveMoneyFlow(navController) }
        composable(Screens.Notifications.route) { NotificationsScreen(navController) }
        //------------------------------------------------------------------

        // More Menu Items ------------------------------------------------
        composable(Screens.TransactionHistory.route) { TransactionHistoryScreen(navController) }
        composable(
            route = Screens.TransactionDetails.route,
            arguments = listOf(navArgument("transactionJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val transactionJson = backStackEntry.arguments?.getString("transactionJson") ?: ""
            val decodedJson = URLDecoder.decode(transactionJson, "UTF-8")

            // Try to parse as TransactionListDataRecord first
            val transaction = try {
                val parsed = Gson().fromJson(decodedJson, TransactionListDataRecord::class.java)
                // Check if it's a valid transaction (has required fields)
                if (parsed?.MerchantRefID != null) parsed else null
            } catch (e: Exception) {
                null
            }

            TransactionDetailsScreen(
                navController = navController,
                transaction = transaction
            )
        }
        //------------------------------------------------------------------

        composable(Screens.Refund.route) { RefundScreen(navController) }
        composable(Screens.RefundTransaction.route) { RefundTransactionScreen(navController) }
        composable(Screens.RefundInitiated.route) { RefundInitiatedScreen(navController) }
        //------------------------------------------------------------------
        composable(Screens.Settings.route) { SettingsScreen(navController) }
        composable(Screens.HelpAndSupport.route) { HelpAndSupportScreen(navController) }
        composable(Screens.Settlement.route) { SettlementScreen(navController) }

        //Misc
        composable(Screens.Splash.route) { SplashScreen(navController) }
        composable(Screens.FcmToken.route) { FcmTokenScreen(navController) }
    }
}

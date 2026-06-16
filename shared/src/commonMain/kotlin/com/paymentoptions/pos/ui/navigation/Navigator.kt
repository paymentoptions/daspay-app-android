package com.paymentoptions.pos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.paymentoptions.pos.auth.AuthEventManager
import com.paymentoptions.pos.network.TransactionListDataRecord
import com.paymentoptions.pos.urlDecode
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.json.Json

/**
 * Root navigation host for the shared Compose Multiplatform UI.
 *
 * Screens are imported from their respective packages.  Each screen must live
 * in shared/commonMain and must NOT use [android.content.Context] directly –
 * use [com.paymentoptions.pos.storage.AppStorage] and
 * [com.paymentoptions.pos.Platform] expect/actual wrappers instead.
 */
@Composable
fun Navigator(
    startDestination: String = Screens.Splash.route,
) {
    val navController = rememberNavController()

    // ── Global auth event handling ─────────────────────────────────────────
    LaunchedEffect(Unit) {
        AuthEventManager.authEvents.collectLatest { event ->
            when (event) {
                is AuthEventManager.AuthEvent.RequireReAuthentication ->
                    navController.navigate(Screens.FingerprintScan.route) {
                        popUpTo(0) { inclusive = true }
                    }
                is AuthEventManager.AuthEvent.RequireManualSignIn ->
                    navController.navigate(Screens.SignIn.route) {
                        popUpTo(0) { inclusive = true }
                    }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {

        // ── Biometrics & Auth ─────────────────────────────────────────────
        composable(Screens.FingerprintScan.route) {
            OpenFingerprintScanScreen(navController)
        }
        composable(Screens.AuthCheck.route) {
            OpenAuthCheckScreen(navController)
        }
        composable(Screens.SignIn.route) {
            OpenSignInScreen(navController)
        }
        composable(Screens.Token.route) {
            OpenTokenScreen(navController)
        }

        // ── Bottom Nav ────────────────────────────────────────────────────
        composable(Screens.Splash.route) {
            OpenSplashScreen(navController)
        }
        composable(Screens.Dashboard.route) {
            OpenDashboardScreen(navController)
        }
        composable(Screens.FoodOrderFlow.route) {
            FoodOrderFlowPlaceholder(navController)
        }
        composable(Screens.ReceiveMoneyFlow.route) {
            ReceiveMoneyFlowPlaceholder(navController)
        }
        composable(Screens.Notifications.route) {
            NotificationsScreenPlaceholder(navController)
        }

        // ── Transaction history ────────────────────────────────────────────
        composable(
            route = "${Screens.TransactionHistory.route}?showBarChart={showBarChart}",
            arguments = listOf(navArgument("showBarChart") { type = NavType.BoolType; defaultValue = false }),
        ) { back ->
            TransactionHistoryScreenPlaceholder(
                navController,
                back.arguments?.getBoolean("showBarChart") ?: false,
            )
        }

        composable(
            route = "${Screens.TransactionReceipt.route}?transactionId={transactionId}&title={title}&amount={amount}&refrenceId={refrenceId}&aggregator={aggregator}&dateString={dateString}",
            arguments = listOf(
                navArgument("transactionId") { type = NavType.StringType; defaultValue = "" },
                navArgument("title")         { type = NavType.StringType; defaultValue = "" },
                navArgument("amount")        { type = NavType.StringType; defaultValue = "" },
                navArgument("refrenceId")    { type = NavType.StringType; defaultValue = "" },
                navArgument("aggregator")    { type = NavType.StringType; defaultValue = "" },
                navArgument("dateString")    { type = NavType.StringType; defaultValue = "" },
            ),
        ) { back ->
            TransactionStatusScreenPlaceholder(
                navController         = navController,
                transactionUuid       = back.arguments?.getString("transactionId") ?: "",
                title                 = back.arguments?.getString("title")         ?: "",
                amount                = back.arguments?.getString("amount")        ?: "",
                dateString            = back.arguments?.getString("dateString")    ?: "",
                referenceId           = back.arguments?.getString("refrenceId")    ?: "",
                aggregator            = back.arguments?.getString("aggregator")    ?: "",
            )
        }

        composable(
            route = Screens.TransactionAction.route,
            arguments = listOf(
                navArgument("transactionJson") { type = NavType.StringType },
                navArgument("action")          { type = NavType.StringType },
            ),
        ) { back ->
            val json   = urlDecode(back.arguments?.getString("transactionJson") ?: "")
            val action = back.arguments?.getString("action") ?: "VOID"
            val txn    = runCatching {
                Json { ignoreUnknownKeys = true }.decodeFromString<TransactionListDataRecord>(json)
            }.getOrNull()

            TransactionActionScreenPlaceholder(navController, txn, action)
        }

        composable(
            route = Screens.TransactionDetails.route,
            arguments = listOf(navArgument("transactionJson") { type = NavType.StringType }),
        ) { back ->
            val json = urlDecode(back.arguments?.getString("transactionJson") ?: "")
            val txn  = runCatching {
                val parsed = Json { ignoreUnknownKeys = true }.decodeFromString<TransactionListDataRecord>(json)
//                if (parsed.MerchantRefID.isNotBlank()) parsed else null
                parsed
            }.getOrNull()
         //   platformLog("Navigator", "Navigating Parsed transaction for details screen: $txn")

            TransactionDetailsScreenPlaceholder(navController, txn)
        }

        // ── Settlement ────────────────────────────────────────────────────
        composable(Screens.Settlement.route)    { SettlementScreenPlaceholder(navController) }
        composable(
            route     = Screens.SettlementAction.route,
            arguments = listOf(navArgument("settleId") { type = NavType.StringType }),
        ) { back ->
            SettlementActionScreenPlaceholder(navController, back.arguments?.getString("settleId") ?: "")
        }

        // ── Misc ─────────────────────────────────────────────────────────
        composable(Screens.QueryScreen.route)    { TransactionFilterPlaceholder(navController) }
        composable(Screens.RefundInitiated.route){ RefundInitiatedScreenPlaceholder(navController) }
        composable(Screens.Settings.route)       { SettingsScreenPlaceholder(navController) }
        composable(Screens.HelpAndSupport.route) { HelpAndSupportScreenPlaceholder(navController) }
        composable(Screens.SendLogs.route)       { SendLogsScreenPlaceholder(navController) }
        composable(Screens.FcmToken.route)       { FcmTokenScreenPlaceholder(navController) }
    }
}

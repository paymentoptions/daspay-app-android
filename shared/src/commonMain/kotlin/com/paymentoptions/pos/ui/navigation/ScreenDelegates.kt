package com.paymentoptions.pos.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.paymentoptions.pos.network.TransactionListDataRecord
import com.paymentoptions.pos.ui.screens.splash.SplashScreen
import com.paymentoptions.pos.ui.screens.authcheck.AuthCheckScreen

/**
 * These functions are thin delegating wrappers.
 *
 * Replace each placeholder body with the real screen composable once you have
 * migrated that screen's source file into shared/commonMain.
 *
 * Example – once SplashScreen is in commonMain:
 *
 *   @Composable
 *   fun SplashScreenPlaceholder(nav: NavHostController) = SplashScreen(nav)
 */

@Composable
fun SplashScreenPlaceholder(nav: NavHostController) = SplashScreen(nav)

@Composable
fun FingerprintScanScreenPlaceholder(nav: NavHostController) = LoadingPlaceholder()

@Composable
fun AuthCheckScreenPlaceholder(nav: NavHostController) = AuthCheckScreen(nav)

@Composable
fun SignInScreenPlaceholder(nav: NavHostController) = LoadingPlaceholder()

@Composable
fun TokenScreenPlaceholder(nav: NavHostController) = LoadingPlaceholder()

@Composable
fun DashboardScreenPlaceholder(nav: NavHostController) = LoadingPlaceholder()

@Composable
fun FoodOrderFlowPlaceholder(nav: NavHostController) = LoadingPlaceholder()

@Composable
fun ReceiveMoneyFlowPlaceholder(nav: NavHostController) = LoadingPlaceholder()

@Composable
fun NotificationsScreenPlaceholder(nav: NavHostController) = LoadingPlaceholder()

@Composable
fun TransactionHistoryScreenPlaceholder(nav: NavHostController, showBarChart: Boolean) = LoadingPlaceholder()

@Composable
fun TransactionStatusScreenPlaceholder(
    navController: NavHostController,
    transactionUuid: String,
    title: String,
    amount: String,
    dateString: String,
    referenceId: String,
    aggregator: String,
) = LoadingPlaceholder()

@Composable
fun TransactionActionScreenPlaceholder(
    nav: NavHostController,
    transaction: TransactionListDataRecord?,
    action: String,
) = LoadingPlaceholder()

@Composable
fun TransactionDetailsScreenPlaceholder(
    nav: NavHostController,
    transaction: TransactionListDataRecord?,
) = LoadingPlaceholder()

@Composable
fun SettlementScreenPlaceholder(nav: NavHostController) = LoadingPlaceholder()

@Composable
fun SettlementActionScreenPlaceholder(nav: NavHostController, settleId: String) = LoadingPlaceholder()

@Composable
fun TransactionFilterPlaceholder(nav: NavHostController) = LoadingPlaceholder()

@Composable
fun RefundInitiatedScreenPlaceholder(nav: NavHostController) = LoadingPlaceholder()

@Composable
fun SettingsScreenPlaceholder(nav: NavHostController) = LoadingPlaceholder()

@Composable
fun HelpAndSupportScreenPlaceholder(nav: NavHostController) = LoadingPlaceholder()

@Composable
fun SendLogsScreenPlaceholder(nav: NavHostController) = LoadingPlaceholder()

@Composable
fun FcmTokenScreenPlaceholder(nav: NavHostController) = LoadingPlaceholder()

// ── Default placeholder ───────────────────────────────────────────────────────

@Composable
private fun LoadingPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

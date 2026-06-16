package com.paymentoptions.pos.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.paymentoptions.pos.network.TransactionListDataRecord
import com.paymentoptions.pos.ui.screens.authcheck.AuthCheckScreen
import com.paymentoptions.pos.ui.screens.dashboard.DashboardScreen
import com.paymentoptions.pos.ui.screens.dashboard.TransactionActionScreen
import com.paymentoptions.pos.ui.screens.fingerprintscan.FingerprintAutoLoginScreen
import com.paymentoptions.pos.ui.screens.helpandsupport.HelpAndSupportScreen
import com.paymentoptions.pos.ui.screens.notifications.NotificationsScreen
import com.paymentoptions.pos.ui.screens.settings.SettingsScreen
import com.paymentoptions.pos.ui.screens.settlement.SettlementActionScreen
import com.paymentoptions.pos.ui.screens.settlement.SettlementScreen
import com.paymentoptions.pos.ui.screens.signin.SignInScreen
import com.paymentoptions.pos.ui.screens.splash.SplashScreen
import com.paymentoptions.pos.ui.screens.token.TokenScreen
import com.paymentoptions.pos.ui.screens.transactiondetails.TransactionDetailsScreen
import com.paymentoptions.pos.ui.screens.transactiondetails.TransactionStatusScreen
import com.paymentoptions.pos.ui.screens.transactionshistory.TransactionHistoryScreen
import com.paymentoptions.pos.ui.screens.filter.TransactionFilter
import com.paymentoptions.pos.ui.screens.logger.SendLogsScreen
import com.paymentoptions.pos.ui.screens._flow.foodOrderFlow.FoodOrderFlow
import com.paymentoptions.pos.ui.screens._flow.receiveMoneyFlow.ReceiveMoneyFlow
import com.paymentoptions.pos.ui.screens._flow.refundFlow.refundinitiated.RefundInitiatedScreen
import com.paymentoptions.pos.utils.TransactionAction

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
fun OpenSplashScreen(nav: NavHostController) = SplashScreen(nav, "DEV")

@Composable
fun OpenFingerprintScanScreen(nav: NavHostController) = FingerprintAutoLoginScreen(nav)

@Composable
fun OpenAuthCheckScreen(nav: NavHostController) = AuthCheckScreen(nav)

@Composable
fun OpenSignInScreen(nav: NavHostController) = SignInScreen(nav)

@Composable
fun OpenTokenScreen(nav: NavHostController) = TokenScreen(nav)

@Composable
fun OpenDashboardScreen(nav: NavHostController) = DashboardScreen(nav)

@Composable
fun FoodOrderFlowPlaceholder(nav: NavHostController) = FoodOrderFlow(nav)

@Composable
fun ReceiveMoneyFlowPlaceholder(nav: NavHostController) = ReceiveMoneyFlow(nav)

@Composable
fun NotificationsScreenPlaceholder(nav: NavHostController) = NotificationsScreen(nav)

@Composable
fun TransactionHistoryScreenPlaceholder(nav: NavHostController, showBarChart: Boolean) = TransactionHistoryScreen(nav, showBarChart)

@Composable
fun TransactionStatusScreenPlaceholder(
    navController: NavHostController,
    transactionUuid: String,
    title: String,
    amount: String,
    dateString: String,
    referenceId: String,
    aggregator: String,
) = TransactionStatusScreen(
    navController = navController,
    transactionUUid = transactionUuid,
    title = title,
    amount = amount,
    dateString = dateString,
    referenceId = referenceId,
    aggregator = aggregator,
)

@Composable
fun TransactionActionScreenPlaceholder(
    nav: NavHostController,
    transaction: TransactionListDataRecord?,
    action: String,
) {
    if (transaction != null) {
        val targetAction = runCatching { TransactionAction.valueOf(action.uppercase()) }
            .getOrDefault(TransactionAction.NONE)
        TransactionActionScreen(nav, transaction, targetAction)
    } else {
        LoadingPlaceholder()
    }
}

@Composable
fun TransactionDetailsScreenPlaceholder(
    nav: NavHostController,
    transaction: TransactionListDataRecord?,
) = if (transaction != null) TransactionDetailsScreen(nav, transaction) else LoadingPlaceholder()

@Composable
fun SettlementScreenPlaceholder(nav: NavHostController) = SettlementScreen(nav)

@Composable
fun SettlementActionScreenPlaceholder(nav: NavHostController, settleId: String) =
    SettlementActionScreen(nav, settleId)

@Composable
fun TransactionFilterPlaceholder(nav: NavHostController) = TransactionFilter(nav)

@Composable
fun RefundInitiatedScreenPlaceholder(nav: NavHostController) = RefundInitiatedScreen(nav)

@Composable
fun SettingsScreenPlaceholder(nav: NavHostController) = SettingsScreen(nav)

@Composable
fun HelpAndSupportScreenPlaceholder(nav: NavHostController) = HelpAndSupportScreen(nav)

@Composable
fun SendLogsScreenPlaceholder(nav: NavHostController) = SendLogsScreen(nav)

@Composable
fun FcmTokenScreenPlaceholder(nav: NavHostController) = LoadingPlaceholder()

// ── Default placeholder ───────────────────────────────────────────────────────

@Composable
private fun LoadingPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

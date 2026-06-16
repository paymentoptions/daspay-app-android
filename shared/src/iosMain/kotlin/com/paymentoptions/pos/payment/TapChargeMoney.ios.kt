package com.paymentoptions.pos.payment

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.paymentoptions.pos.showToast

@Composable
actual fun TapChargeMoney(
    navController: NavController,
    amountToCharge: String,
    onLoader: (nextStage: () -> Unit) -> Unit,
    onSuccessUpdateFlowStage: () -> Unit,
    onFailureUpdateFlowStage: () -> Unit,
    updateLatestTransaction: (id: String) -> Unit,
) {
    showToast("Tap to Pay is only available on Android")
}

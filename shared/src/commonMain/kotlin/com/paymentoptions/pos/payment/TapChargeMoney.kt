package com.paymentoptions.pos.payment

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
expect fun TapChargeMoney(
    navController: NavController,
    amountToCharge: String,
    onLoader: (nextStage: () -> Unit) -> Unit = {},
    onSuccessUpdateFlowStage: () -> Unit = {},
    onFailureUpdateFlowStage: () -> Unit = {},
    updateLatestTransaction: (id: String) -> Unit = {},
)

package com.paymentoptions.pos.payment

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.screens.status.StatusScreenType

@Composable
expect fun TapChargeMoney(
    navController: NavController,
    amountToCharge: String,
    gatewayNotes: String?,
    /** Signals that the real card-read-to-backend-confirmation processing window has started. */
    onLoader: () -> Unit = {},
    onSuccessUpdateFlowStage: () -> Unit = {},
    /** [outcome] is one of DECLINED/TIMEOUT/CANCELLED/ERROR — never SUCCESS/PROCESSING (5.9). */
    onFailureUpdateFlowStage: (outcome: StatusScreenType) -> Unit = {},
    updateLatestTransaction: (id: String) -> Unit = {},
    onFailureMessage: (String) -> Unit = {},
    onMandatorySignature: () -> Unit,
)

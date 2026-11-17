package com.paymentoptions.pos.payment

import com.paymentoptions.pos.ui.screens._flow.foodOrderFlow.Cart

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
    /** Called instead of success when the amount needs a signature; null = no signature step here. */
    onMandatorySignature: (() -> Unit)? = null,
    /** Food-order cart, registered as a pay-by-link product and sent as the payment's cartID. */
    cart: Cart? = null,
)

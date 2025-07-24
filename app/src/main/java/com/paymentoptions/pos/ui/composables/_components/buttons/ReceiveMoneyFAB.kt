package com.paymentoptions.pos.ui.composables._components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.RECEIVE_MONEY_BUTTON_HEIGHT_IN_DP
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.enabledFilledButtonGradientBrush


@Composable
fun ReceiveMoneyFAB(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = Icons.Default.Add,
        contentDescription = "Receive Money",
        tint = Color.White,
        modifier = modifier
            .size(RECEIVE_MONEY_BUTTON_HEIGHT_IN_DP)
            .background(brush = enabledFilledButtonGradientBrush, shape = RoundedCornerShape(50))
            .padding(10.dp)
            .clickable { navController.navigate(Screens.ReceiveMoneyFlow.route) }

    )
}
package com.paymentoptions.pos.ui.composables.screens.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables._components.NotificationPermission
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun DashboardScreen(navController: NavController) {

    NotificationPermission()
    SectionedLayout(
        navController = navController, bottomSectionContent = {
            BottomSectionContent(navController)
        }, defaultBottomSectionPaddingInDp = 0.dp
    )
}
package com.paymentoptions.pos.ui.composables.screens.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables._components.NotificationPermission
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun DashboardScreen(navController: NavController) {

    val enableScrollingInsideBottomSectionContent = true

    NotificationPermission()
    SectionedLayout(
        navController = navController,
        defaultBottomSectionPaddingInDp = 0.dp,
        enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent
    ) {
        BottomSectionContent(
            navController,
            enableScrolling = enableScrollingInsideBottomSectionContent
        )
    }
}
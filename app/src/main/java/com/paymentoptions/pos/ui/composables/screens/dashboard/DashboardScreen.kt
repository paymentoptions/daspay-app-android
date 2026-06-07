package com.paymentoptions.pos.ui.composables.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.device.DPSharedPreferences
import com.paymentoptions.pos.services.analytics.AppAnalytics
import com.paymentoptions.pos.ui.composables._components.NotificationPermission
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun DashboardScreen(navController: NavController) {

    val enableScrollingInsideBottomSectionContent = true
    val context = LocalContext.current
    var showOverlay by remember {
        mutableStateOf(
            DPSharedPreferences.getBoolean(context = context, "dashboard_overlay_shown").not()
        )
    }

    LaunchedEffect(Unit) {
        AppAnalytics.dashboardNavigation(destination = "Dashboard", source = "screen_enter")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Dashboard content
        NotificationPermission()
        SectionedLayout(
            navController = navController,
            bottomBarContent = BottomBarContent.NAVIGATION_BAR,
            bottomSectionMinHeightRatio = 0.9f,
            bottomSectionMaxHeightRatio = 0.9f,
            bottomSectionPaddingInDp = 0.dp,
            enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent
        ) {
            BottomSectionContent(
                navController,
                enableScrolling = enableScrollingInsideBottomSectionContent
            )
        }

        // show Overlay only for first time
        if (showOverlay) {
            OverLayScreen(context) {
                showOverlay = false
            }
        }
    }
}
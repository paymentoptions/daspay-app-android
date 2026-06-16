package com.paymentoptions.pos.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.ui.composables._components.NotificationPermission
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun DashboardScreen(navController: NavController) {
    val enableScrollingInsideBottomSectionContent = true
    var showOverlay by remember {
        mutableStateOf(DPStorageManager.getBoolean("dashboard_overlay_shown").not())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotificationPermission()
        SectionedLayout(
            navController = navController,
            bottomBarContent = BottomBarContent.NAVIGATION_BAR,
            bottomSectionMinHeightRatio = 0.9f,
            bottomSectionMaxHeightRatio = 0.9f,
            bottomSectionPaddingInDp = 0.dp,
            enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent,
        ) {
            BottomSectionContent(
                navController,
                enableScrolling = enableScrollingInsideBottomSectionContent,
            )
        }

        if (showOverlay) {
            OverLayScreen {
                showOverlay = false
            }
        }
    }
}
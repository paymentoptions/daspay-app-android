package com.paymentoptions.pos.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.payment.reader.ReaderState
import com.paymentoptions.pos.payment.reader.getTapToPayReader
import com.paymentoptions.pos.tapToPayDisplayName
import com.paymentoptions.pos.ui.composables._components.NotificationPermission
import com.paymentoptions.pos.ui.composables._components.dialogs.MyDialog
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout
import com.paymentoptions.pos.ui.composables.tapToPay.EnableTapToPayFlow

private const val KEY_TTP_ENABLE_PROMPT_DISMISSED = "ttp_enable_prompt_dismissed"

@Composable
fun DashboardScreen(navController: NavController) {
    val enableScrollingInsideBottomSectionContent = true
    var showOverlay by remember {
        mutableStateOf(DPStorageManager.getBoolean("dashboard_overlay_shown").not())
    }

    val reader = remember { getTapToPayReader() }
    val readerState by reader.state.collectAsState()
    var showEnableTapToPayPrompt by remember {
        mutableStateOf(!DPStorageManager.getBoolean(KEY_TTP_ENABLE_PROMPT_DISMISSED))
    }
    var showEnableTapToPayFlow by remember { mutableStateOf(false) }

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

    MyDialog(
        showDialog = showEnableTapToPayPrompt && readerState is ReaderState.NotLinked,
        title = tapToPayDisplayName,
        text = "$tapToPayDisplayName isn't set up yet. Enable it to start accepting contactless payments.",
        acceptButtonText = "Enable",
        cancelButtonText = "Not Now",
        onAcceptFn = {
            showEnableTapToPayPrompt = false
            showEnableTapToPayFlow = true
        },
        onDismissFn = {
            showEnableTapToPayPrompt = false
            DPStorageManager.saveBoolean(KEY_TTP_ENABLE_PROMPT_DISMISSED, true)
        },
    )

    if (showEnableTapToPayFlow) {
        EnableTapToPayFlow(onDismiss = { showEnableTapToPayFlow = false })
    }
}
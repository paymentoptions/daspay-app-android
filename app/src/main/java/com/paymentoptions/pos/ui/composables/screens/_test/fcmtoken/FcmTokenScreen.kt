package com.paymentoptions.pos.ui.composables.screens._test.fcmtoken

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun FcmTokenScreen(navController: NavController) {
    SectionedLayout(
        navController = navController, bottomSectionContent = {
            BottomSectionContent(navController)
        })
}
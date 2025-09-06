package com.paymentoptions.pos.ui.composables.screens.token

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun TokenScreen(navController: NavController) {
    val enableScrollingInsideBottomSectionContent = true

    SectionedLayout(
        navController = navController,
        enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent,
        bottomBarContent = BottomBarContent.NOTHING
    ) {
        BottomSectionContent(
            navController, enableScrolling = enableScrollingInsideBottomSectionContent
        )
    }
}
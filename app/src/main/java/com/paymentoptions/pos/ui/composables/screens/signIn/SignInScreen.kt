package com.paymentoptions.pos.ui.composables.screens.signIn

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun SignInScreen(navController: NavController) {
    val scrollingInsideBottomSectionContent = true

    SectionedLayout(
        navController = navController,
        enableScrollingOfBottomSectionContent = !scrollingInsideBottomSectionContent
    ) {
        BottomSectionContent(navController, enableScrolling = scrollingInsideBottomSectionContent)
    }
}
package com.paymentoptions.pos.ui.screens.signin

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout
import com.paymentoptions.pos.ui.screens.signin.SignInBottomSectionContent

@Composable
fun SignInScreen(navController: NavController) {
    val scrollingInsideBottomSectionContent = true

    SectionedLayout(
        navController = navController,
        enableScrollingOfBottomSectionContent = !scrollingInsideBottomSectionContent,
        bottomBarContent = BottomBarContent.NOTHING,
    ) {
        SignInBottomSectionContent(navController, enableScrolling = scrollingInsideBottomSectionContent)
    }
}


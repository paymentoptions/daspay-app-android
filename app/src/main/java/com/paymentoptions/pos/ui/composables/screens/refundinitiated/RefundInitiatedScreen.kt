package com.paymentoptions.pos.ui.composables.screens.refundinitiated

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables._components.images.CreditCardImage
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun RefundInitiatedScreen(navController: NavController) {
    val enableScrollingInsideBottomSectionContent = false

    SectionedLayout(
        navController = navController,
        bottomBarContent = BottomBarContent.TOGGLE_BUTTON,
        bottomSectionPaddingInDp = 0.dp,
        bottomSectionMaxHeightRatio = 0.95f,
        imageBelowLogo = {
            CreditCardImage(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(
                        shape = RoundedCornerShape(16.dp)
                    )
            )
        },
        enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent
    ) {
        BottomSectionContent(
            navController, enableScrolling = enableScrollingInsideBottomSectionContent
        )
    }
}
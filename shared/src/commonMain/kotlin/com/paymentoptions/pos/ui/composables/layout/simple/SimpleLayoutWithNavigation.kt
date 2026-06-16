package com.paymentoptions.pos.ui.composables.layout.simple

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables._components.BottomNavShape
import com.paymentoptions.pos.ui.composables._components.buttons.ReceiveMoneyFAB
import com.paymentoptions.pos.ui.composables._components.images.BackgroundImage
import com.paymentoptions.pos.ui.composables._components.images.LogoImage
import com.paymentoptions.pos.ui.composables.layout.sectioned.BOTTOM_NAVIGATION_HEIGHT_IN_DP
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_HEIGHT_IN_DP
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_TOP_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.layout.sectioned.MyBottomNavigationBar
import com.paymentoptions.pos.ui.theme.innerShadow
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.modifiers.conditional
import com.paymentoptions.pos.utils.modifiers.innerShadow


@Composable
fun SimpleLayoutWithNavigation(
    navController: NavController,
    blurTopSection: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var showMoreItems by remember { mutableStateOf(false) }
    val overlayColor = Color.Black.copy(alpha = if (showMoreItems) 0.8f else 0.05f)
    val borderRadiusInDp = 32.dp

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        BackgroundImage(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
        )

        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = LOGO_TOP_PADDING_IN_DP)
                .zIndex(2f)
        ) {
            LogoImage(
                blurTopSection = blurTopSection,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LOGO_HEIGHT_IN_DP)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .align(alignment = Alignment.TopCenter)
                .padding(bottom = BOTTOM_NAVIGATION_HEIGHT_IN_DP)
                .zIndex(2f)
                .clickable(
                    enabled = !showMoreItems,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { })
        ) {
            content()

            //Just an overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(overlayColor)
                    .zIndex(3f)
            )
        }

        if (showMoreItems) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = BOTTOM_NAVIGATION_HEIGHT_IN_DP)
                    .zIndex(3f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showMoreItems = false
                    }
            )
        }

        //Bottom Navigation Bar
        ReceiveMoneyFAB(
            navController,
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .offset(y = BOTTOM_NAVIGATION_HEIGHT_IN_DP.times(-0.6f))
                .zIndex(5f)
                .shadow(
                    30.dp,
                    shape = RoundedCornerShape(50),
                    spotColor = primary900,
                    ambientColor = Color.Black
                )
        )

        Row(
            modifier = Modifier
                .background(Color.White)
                .background(overlayColor)
                .clip(
                    RoundedCornerShape(
                        topStart = if (showMoreItems) borderRadiusInDp else 20.dp,
                        topEnd = if (showMoreItems) borderRadiusInDp else 20.dp
                    )
                )
                .background(if (showMoreItems) Color.White else Color.Transparent)
                .conditional(showMoreItems) { background(primary100.copy(alpha = 0.04f)) }
                .align(alignment = Alignment.BottomCenter)
                .zIndex(4f)
                .conditional(showMoreItems) {
                    innerShadow(
                        color = innerShadow,
                        blur = 20.dp,
                        spread = 10.dp,
                        cornersRadius = 0.dp,
                        offsetX = 0.dp,
                        offsetY = 0.dp
                    )
                }

        ) {

            val modifier1 = if (showMoreItems) Modifier else Modifier.clip(
                BottomNavShape(
                    cornerRadius = with(LocalDensity.current) { 20.dp.toPx() },
                    dockRadius = with(LocalDensity.current) { 38.dp.toPx() },
                )
            )

            MyBottomNavigationBar(
                navController, modifier = modifier1, showMoreItems, onClickShowMoreItems = {
                    showMoreItems = !showMoreItems
                }, bottomNavigationBarHeightInDp = BOTTOM_NAVIGATION_HEIGHT_IN_DP
            )
        }
    }
}
package com.paymentoptions.pos.ui.composables.layout.sectioned

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables._components.BottomNavShape
import com.paymentoptions.pos.ui.composables._components.buttons.BackButton
import com.paymentoptions.pos.ui.composables._components.buttons.ReceiveMoneyFAB
import com.paymentoptions.pos.ui.composables._components.images.BackgroundImage
import com.paymentoptions.pos.ui.composables._components.images.LogoImage
import com.paymentoptions.pos.ui.composables._components.images.TapToPayImage
import com.paymentoptions.pos.ui.theme.primary100

val RECEIVE_MONEY_BUTTON_HEIGHT_IN_DP = 60.dp
val DEFAULT_BOTTOM_SECTION_PADDING_IN_DP = 16.dp

@Composable
fun SectionedLayout(
    navController: NavController,
    bottomSectionContent: @Composable () -> Unit,
    bottomSectionMinHeightRatio: Float = 0.2f,
    bottomSectionMaxHeightRatio: Float = 0.8f,
    enableBottomNavigationBar: Boolean = true,
    showBackButton: Boolean = false,
    defaultBottomSectionPaddingInDp: Dp = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP,
) {
    val bottomSectionMinHeightDp =
        Dp(LocalConfiguration.current.screenHeightDp.times(bottomSectionMinHeightRatio))
    val bottomSectionMaxHeightDp =
        Dp(LocalConfiguration.current.screenHeightDp.times(bottomSectionMaxHeightRatio))
    var showMoreItems by remember { mutableStateOf(false) }

    val overlayColor = Color.Black.copy(alpha = if (showMoreItems) 0.8f else 0.05f)
    val borderRadiusInDp = 32.dp
    val scrollState = rememberScrollState()

    fun toggleShowMoreItems() {
        showMoreItems = !showMoreItems
    }

    Box(modifier = Modifier.fillMaxSize()) {

        BackgroundImage(modifier = Modifier.zIndex(1f))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .align(alignment = Alignment.TopCenter)
                .padding(bottom = if (enableBottomNavigationBar) BOTTOM_NAVIGATION_HEIGHT_IN_DP else 0.dp)
                .zIndex(2f)
                .clickable(
                    enabled = !showMoreItems,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { })
        ) {

            //Top Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 70.dp)
                    .align(alignment = Alignment.TopCenter)
                    .zIndex(1f)
            ) {
                LogoImage()
                Spacer(modifier = Modifier.height(35.dp))
                TapToPayImage(height = 280.dp)
            }

            //Bottom Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .heightIn(bottomSectionMinHeightDp, bottomSectionMaxHeightDp)
                    .verticalScroll(scrollState)
                    .align(alignment = Alignment.BottomCenter)
                    .zIndex(2f)
                    .background(color = Color.Transparent)
                    .clip(
                        RoundedCornerShape(
                            topStart = borderRadiusInDp, topEnd = borderRadiusInDp
                        )
                    )
                    .background(color = Color.White)
                    .padding(
                        start = defaultBottomSectionPaddingInDp,
                        top = defaultBottomSectionPaddingInDp,
                        end = defaultBottomSectionPaddingInDp,
                        bottom = if (enableBottomNavigationBar) defaultBottomSectionPaddingInDp.plus(
                            20.dp
                        ) else if (showBackButton) 0.dp else defaultBottomSectionPaddingInDp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center

            ) {
                bottomSectionContent()

                if (showBackButton) {
                    Spacer(modifier = Modifier.height(20.dp))
                    BackButton(onClickShowMenuBarButton = { navController.popBackStack() })
                }
            }

            //Just an overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(overlayColor)
                    .zIndex(3f)
            )
        }

        //Bottom Navigation Bar
        if (enableBottomNavigationBar) {
            ReceiveMoneyFAB(
                navController,
                modifier = Modifier
                    .align(alignment = Alignment.BottomCenter)
                    .zIndex(5f)
                    .padding(bottom = BOTTOM_NAVIGATION_HEIGHT_IN_DP.div(2).plus(5.dp))
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
                    .background(primary100.copy(alpha = 0.04f))
                    .align(alignment = Alignment.BottomCenter)
                    .zIndex(4f)

            ) {

                val modifier = if (showMoreItems) Modifier else Modifier.clip(
                    BottomNavShape(
                        cornerRadius = with(LocalDensity.current) { 20.dp.toPx() },
                        dockRadius = with(LocalDensity.current) { 38.dp.toPx() },
                    )
                )

                MyBottomNavigationBar(
                    navController, modifier = modifier, showMoreItems, onClickShowMoreItems = {
                        toggleShowMoreItems()
                    }, BOTTOM_NAVIGATION_HEIGHT_IN_DP
                )
            }
        }
    }
}

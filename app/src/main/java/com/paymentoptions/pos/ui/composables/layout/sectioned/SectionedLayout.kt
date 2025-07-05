package com.paymentoptions.pos.ui.composables.layout.sectioned

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
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
import com.paymentoptions.pos.ui.composables._components.images.BackgroundImage
import com.paymentoptions.pos.ui.composables._components.images.LogoImage
import com.paymentoptions.pos.ui.composables._components.images.TapToPayImage
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.primary100

val RECEIVE_MONEY_BUTTON_HEIGHT_IN_DP = 60.dp

@Composable
fun SectionedLayout(
    navController: NavController,
    bottomSectionContent: @Composable () -> Unit,
    bottomSectionMinHeightRatio: Float = 0.2f,
    bottomSectionMaxHeightRatio: Float = 0.73f,
    enableBottomNavigationBar: Boolean = true,
    defaultBottomSectionPadding: Dp = 16.dp,
) {

    val bottomSectionMinHeightDp =
        Dp(LocalConfiguration.current.screenHeightDp.times(bottomSectionMinHeightRatio))
    val bottomSectionMaxHeightDp =
        Dp(LocalConfiguration.current.screenHeightDp.times(bottomSectionMaxHeightRatio))
    var showMoreItems by remember { mutableStateOf(false) }

    val bottomNavigationBarHeightInDp = BOTTOM_NAVIGATION_HEIGHT_IN_DP
    val overlayColor = Color.Black.copy(alpha = if (showMoreItems) 0.8f else 0.05f)
    val borderRadiusInDp = 32.dp

    fun toggleShowMoreItems() {
        showMoreItems = !showMoreItems
    }

    Box(modifier = Modifier.fillMaxSize()) {

        BackgroundImage(modifier = Modifier.zIndex(1f))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .align(alignment = Alignment.TopCenter)
                .padding(bottom = if (enableBottomNavigationBar) bottomNavigationBarHeightInDp else 0.dp)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .heightIn(bottomSectionMinHeightDp, bottomSectionMaxHeightDp)
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
                        start = defaultBottomSectionPadding,
                        top = defaultBottomSectionPadding,
                        end = defaultBottomSectionPadding,
                        bottom = defaultBottomSectionPadding.plus(if (enableBottomNavigationBar) 20.dp else 0.dp)
                    )

            ) {
                bottomSectionContent()
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
            OverlayReceiveMoneyButton(
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
                    }, bottomNavigationBarHeightInDp
                )
            }
        }
    }
}

@Composable
fun OverlayReceiveMoneyButton(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = Icons.Default.Add,
        contentDescription = "Receive Money",
        tint = Color.White,
        modifier = modifier
            .size(RECEIVE_MONEY_BUTTON_HEIGHT_IN_DP)
            .background(primary100, shape = RoundedCornerShape(50))
            .clickable {
                navController.navigate(Screens.ReceiveMoney.route)
            })

}
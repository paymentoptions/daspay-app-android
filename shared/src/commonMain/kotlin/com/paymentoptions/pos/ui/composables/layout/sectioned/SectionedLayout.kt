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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.paymentoptions.pos.device.ScreenRatioToDp
import com.paymentoptions.pos.ui.composables._components.BottomNavShape
import com.paymentoptions.pos.ui.composables._components.ZigZagContainer2
import com.paymentoptions.pos.ui.composables._components.buttons.ReceiveMoneyFAB
import com.paymentoptions.pos.ui.composables._components.buttons.ToggleBottomNavigationBarButton
import com.paymentoptions.pos.ui.composables._components.images.BackgroundImage
import com.paymentoptions.pos.ui.composables._components.images.LogoImage
import com.paymentoptions.pos.ui.composables._components.images.TapToPayImage
import com.paymentoptions.pos.ui.theme.innerShadow
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.modifiers.conditional
import com.paymentoptions.pos.utils.modifiers.innerShadow

@Composable
fun SectionedLayout(
    navController: NavController,
    bottomSectionMinHeightRatio: Float = 0.1f,
    bottomSectionMaxHeightRatio: Float = 0.9f,
    bottomBarContent: BottomBarContent,
    bottomSectionPaddingInDp: Dp = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP,
    alwaysShowLogo: Boolean = true,
    imageBelowLogo: @Composable () -> Unit = {
        TapToPayImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )
    },
    enableScrollingOfBottomSectionContent: Boolean = true,
    blurTopSection: Boolean = false,
    enableZigZagContainerForBottomSection: Boolean = false,
    bottomSectionContent: @Composable () -> Unit = {},
) {
    var bottomBarContentState by remember { mutableStateOf(bottomBarContent) }

    val bottomSectionMinHeightDp = ScreenRatioToDp(bottomSectionMinHeightRatio)
    val bottomSectionMaxHeightDp = ScreenRatioToDp(bottomSectionMaxHeightRatio)

    var showMoreItems by remember { mutableStateOf(false) }

    val overlayColor = Color.Black.copy(alpha = if (showMoreItems) 0.8f else 0.05f)
    val borderRadiusInDp = 32.dp
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        BackgroundImage(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .align(alignment = Alignment.TopCenter)
                .padding(bottom = if (bottomBarContentState === BottomBarContent.NAVIGATION_BAR) BOTTOM_NAVIGATION_HEIGHT_IN_DP else 0.dp)
                .zIndex(2f)
                .clickable(
                    enabled = !showMoreItems,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { })
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = LOGO_TOP_PADDING_IN_DP)
                    .align(alignment = Alignment.TopCenter)
                    .zIndex(1f)
                    .conditional(blurTopSection) {
                        blur(radius = 8.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                    }) {
                LogoImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LOGO_HEIGHT_IN_DP)
                )

                Spacer(modifier = Modifier.height(20.dp))

                imageBelowLogo()
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = if (alwaysShowLogo) LOGO_TOP_PADDING_IN_DP.times(1.5f)
                            .plus(LOGO_HEIGHT_IN_DP) else 0.dp
                    )
                    .height(IntrinsicSize.Min)
                    .heightIn(bottomSectionMinHeightDp, bottomSectionMaxHeightDp)
                    .align(alignment = Alignment.BottomCenter)
                    .zIndex(2f)
                    .conditional(!enableZigZagContainerForBottomSection) {
                        clip(
                            RoundedCornerShape(
                                topStart = borderRadiusInDp, topEnd = borderRadiusInDp
                            )
                        )
                    }
                    .background(color = Color.White)
                    .drawBehind {
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(0f, size.height - 25.dp.toPx()),
                            size = Size(size.width, 25.dp.toPx()),
                            blendMode = BlendMode.Darken
                        )
                    }
                    .innerShadow(
                        color = innerShadow,
                        blur = 20.dp,
                        spread = 10.dp,
                        cornersRadius = 0.dp,
                        offsetX = 0.dp,
                        offsetY = 0.dp,
                        showBottom = false
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ZigZagContainer2(enabled = enableZigZagContainerForBottomSection)
                {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(
                                start = if (enableZigZagContainerForBottomSection) 0.dp else bottomSectionPaddingInDp,
                                top = bottomSectionPaddingInDp,
                                end = if (enableZigZagContainerForBottomSection) 0.dp else bottomSectionPaddingInDp,
                                bottom = when (bottomBarContentState) {
                                    BottomBarContent.NAVIGATION_BAR -> bottomSectionPaddingInDp.plus(
                                        15.dp
                                    )

                                    BottomBarContent.TOGGLE_BUTTON -> 20.dp
                                    BottomBarContent.NOTHING -> 20.dp
                                }
                            )
                            .conditional(enableScrollingOfBottomSectionContent) {
                                verticalScroll(scrollState)
                            }) {
                        bottomSectionContent()
                    }
                }

                if (bottomBarContentState === BottomBarContent.TOGGLE_BUTTON) ToggleBottomNavigationBarButton(
                    onClick = { bottomBarContentState = BottomBarContent.NAVIGATION_BAR })
            }

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

        if (bottomBarContentState === BottomBarContent.NAVIGATION_BAR) {
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

                val mod = if (showMoreItems) Modifier else Modifier.clip(
                    BottomNavShape(
                        cornerRadius = with(LocalDensity.current) { 20.dp.toPx() },
                        dockRadius = with(LocalDensity.current) { 38.dp.toPx() },
                    )
                )

                MyBottomNavigationBar(
                    navController, modifier = mod, showMoreItems, onClickShowMoreItems = {
                        showMoreItems = !showMoreItems
                    }, bottomNavigationBarHeightInDp = BOTTOM_NAVIGATION_HEIGHT_IN_DP
                )
            }
        }
    }
}

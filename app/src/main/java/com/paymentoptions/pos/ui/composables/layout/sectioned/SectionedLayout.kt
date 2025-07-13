package com.paymentoptions.pos.ui.composables.layout.sectioned

import android.annotation.SuppressLint
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
import androidx.compose.material3.Scaffold
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

val LOGO_TOP_PADDING_IN_DP = 50.dp
val LOGO_HEIGHT_IN_DP = 60.dp
val RECEIVE_MONEY_BUTTON_HEIGHT_IN_DP = 60.dp
val DEFAULT_BOTTOM_SECTION_PADDING_IN_DP = 16.dp

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun SectionedLayout(
    navController: NavController,
    bottomSectionContent: @Composable () -> Unit,
    bottomSectionMinHeightRatio: Float = 0.1f,
    bottomSectionMaxHeightRatio: Float = .8f,
    showBottomNavigationBar: Boolean = true,
    showBackButton: Boolean = false,
    defaultBottomSectionPaddingInDp: Dp = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP,
    alwaysShowLogo: Boolean = true,
    imageBelowLogo: @Composable () -> Unit = {
        TapToPayImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )
    },
) {

    val configuration = LocalConfiguration.current

    val bottomSectionMinHeightDp =
        configuration.screenHeightDp.dp.times(bottomSectionMinHeightRatio)
    var bottomSectionMaxHeightDp =
        configuration.screenHeightDp.dp.times(bottomSectionMaxHeightRatio)


    var showMoreItems by remember { mutableStateOf(false) }

//    println("configuration: ${configuration.screenHeightDp} | $bottomSectionMinHeightDp | $bottomSectionMaxHeightDp")

    val overlayColor = Color.Black.copy(alpha = if (showMoreItems) 0.8f else 0.05f)
    val borderRadiusInDp = 32.dp
    val scrollState = rememberScrollState()

    fun toggleShowMoreItems() {
        showMoreItems = !showMoreItems
    }

    Scaffold {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
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
                    .padding(bottom = if (showBottomNavigationBar) BOTTOM_NAVIGATION_HEIGHT_IN_DP else 0.dp)
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
                        .padding(top = LOGO_TOP_PADDING_IN_DP)
                        .align(alignment = Alignment.TopCenter)
                        .zIndex(1f)
                ) {
                    LogoImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(LOGO_HEIGHT_IN_DP)
                    )
                    Spacer(modifier = Modifier.height(25.dp))

                    imageBelowLogo()

                }

                //Bottom Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = if (alwaysShowLogo) LOGO_TOP_PADDING_IN_DP.times(1.6f)
                                .plus(LOGO_HEIGHT_IN_DP) else 0.dp
                        )
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
                            bottom = if (showBottomNavigationBar) defaultBottomSectionPaddingInDp.plus(
                                25.dp
                            ) else if (showBackButton) 0.dp else defaultBottomSectionPaddingInDp.plus(
                                10.dp
                            )
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top

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
            if (showBottomNavigationBar) {
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
}

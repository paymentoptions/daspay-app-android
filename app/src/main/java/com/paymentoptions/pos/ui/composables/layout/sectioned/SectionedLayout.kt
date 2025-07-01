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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables._components.images.BackgroundImage
import com.paymentoptions.pos.ui.composables._components.images.LogoImage
import com.paymentoptions.pos.ui.composables._components.images.TapToPayImage
import com.paymentoptions.pos.ui.theme.primary100

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

    val bottomNavigationBarHeight = 75.dp
    val bottomNavigationBarExpandedHeight = 348.dp

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
                .padding(bottom = if (enableBottomNavigationBar) bottomNavigationBarHeight else 0.dp)
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
                TapToPayImage(height = 260.dp)
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
                    .padding(defaultBottomSectionPadding)
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
        if (enableBottomNavigationBar) Row(
            modifier = Modifier
                .background(Color.White)
                .background(overlayColor)
                .clip(
                    RoundedCornerShape(
                        topStart = if (showMoreItems) borderRadiusInDp else 0.dp,
                        topEnd = if (showMoreItems) borderRadiusInDp else 0.dp
                    )
                )
                .background(Color.White)
                .background(primary100.copy(alpha = 0.04f))
                .align(alignment = Alignment.BottomCenter)
                .height(if (showMoreItems) bottomNavigationBarExpandedHeight else bottomNavigationBarHeight)
                .zIndex(5f)
        ) {
            MyBottomNavigationBar(navController, showMoreItems, onClickShowMoreItems = {
                toggleShowMoreItems()
            })
        }
    }
}
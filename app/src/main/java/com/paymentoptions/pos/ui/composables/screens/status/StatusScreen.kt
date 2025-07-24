package com.paymentoptions.pos.ui.composables.screens.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.R
import com.paymentoptions.pos.ui.composables._components.images.LogoImage
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_HEIGHT_IN_DP
import com.paymentoptions.pos.ui.composables.layout.simple.SimpleLayout
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.red300
import com.paymentoptions.pos.utils.modifiers.conditional

enum class StatusScreenType {
    ERROR, PROCESSING, SUCCESS
}

data class MessageForStatusScreen(
    val text: String,
    val statusScreenType: StatusScreenType,
)

@Composable
fun StatusScreen(
    navController: NavController,
    dataMessage: MessageForStatusScreen,
    strategyFn: () -> Unit = {},
) {
    Icons.Filled.Check

    SimpleLayout {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .conditional(dataMessage.statusScreenType == StatusScreenType.ERROR) {
                    background(red300)
                }) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(3f)
            ) {
                LogoImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LOGO_HEIGHT_IN_DP)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(7f)
            ) {
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(50)
                        ), contentAlignment = Alignment.Center
                ) {

                    Box(
                        modifier = Modifier
                            .size(if (dataMessage.statusScreenType == StatusScreenType.PROCESSING) 255.dp else 235.dp)
                            .background(
                                color = Color(if (dataMessage.statusScreenType == StatusScreenType.ERROR) 0xFFFFC6C7 else 0xFFA2C3F4).copy(
                                    alpha = 0.5f
                                ), shape = RoundedCornerShape(50)
                            ), contentAlignment = Alignment.Center
                    ) {

                        Box(
                            modifier = Modifier
                                .size(if (dataMessage.statusScreenType == StatusScreenType.PROCESSING) 186.dp else 105.dp)
                                .background(
                                    color = Color(
                                        if (dataMessage.statusScreenType == StatusScreenType.ERROR) 0xFFCD5557 else 0xFF87AEF2
                                    ), shape = RoundedCornerShape(50)
                                ), contentAlignment = Alignment.Center
                        ) {
                            if (dataMessage.statusScreenType != StatusScreenType.PROCESSING) Icon(
                                painter = painterResource(if (dataMessage.statusScreenType == StatusScreenType.ERROR) R.drawable.error else R.drawable.check),
                                contentDescription = "Icon",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(if (dataMessage.statusScreenType == StatusScreenType.ERROR) 78.dp else 50.dp)
                                    .background(Color.Transparent)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = dataMessage.text, style = AppTheme.typography.status
                )
            }
        }
    }

    strategyFn()
}
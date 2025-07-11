package com.paymentoptions.pos.ui.composables.screens.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables._components.images.LogoImage
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_HEIGHT_IN_DP
import com.paymentoptions.pos.ui.composables.layout.simple.SimpleLayout
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.primary900

@Composable
fun StatusScreen(navController: NavController, strategyFn: () -> Unit) {

    val text = "Processing..."
    val icon: ImageVector? = Icons.Filled.Check

    SimpleLayout {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()
        ) {

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
                        .size(280.dp)
                        .background(
                            color = primary900.copy(alpha = 0.4f), shape = RoundedCornerShape(50)
                        ), contentAlignment = Alignment.Center
                ) {

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(color = Color(0xFF87AEF2), shape = RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (icon != null) Icon(
                            imageVector = icon,
                            contentDescription = "Icon",
                            tint = Color.White,
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFA2C3F4), shape = RoundedCornerShape(50)
                                )
                                .padding(40.dp)
                                .size(52.dp),
                        )
                        else Box(
                            modifier = Modifier
                                .size(160.dp)
                                .background(
                                    color = Color(0xFF87AEF2), shape = RoundedCornerShape(50)
                                ), contentAlignment = Alignment.Center
                        ) {
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = text, style = AppTheme.typography.status
                )
            }
        }
    }

    strategyFn()
}
//package com.paymentoptions.pos.ui.composables.screens.status
//
//import androidx.compose.animation.core.RepeatMode
//import androidx.compose.animation.core.animateFloat
//import androidx.compose.animation.core.infiniteRepeatable
//import androidx.compose.animation.core.rememberInfiniteTransition
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Icon
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.draw.scale
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.paymentoptions.pos.R
//import com.paymentoptions.pos.ui.theme.AppTheme
//import com.paymentoptions.pos.ui.theme.red300
//import com.paymentoptions.pos.utils.modifiers.conditional
//
//
//@Composable
//fun SettlementStatusScreen(
//    navController: NavController,
//    dataMessage: MessageForStatusScreen,
//) {
//    // Animate the circles during PROCESSING state
//    val infiniteTransition = rememberInfiniteTransition(label = "processing_animation")
//
//    val pulseScale by infiniteTransition.animateFloat(
//        initialValue = 1f,
//        targetValue = 1.1f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(1000),
//            repeatMode = RepeatMode.Reverse
//        ),
//        label = "pulse_scale"
//    )
//
//    val pulseAlpha by infiniteTransition.animateFloat(
//        initialValue = 0.3f,
//        targetValue = 0.6f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(1000),
//            repeatMode = RepeatMode.Reverse
//        ),
//        label = "pulse_alpha"
//    )
//
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier
//            .fillMaxSize()
//            .conditional(dataMessage.statusScreenType == StatusScreenType.ERROR) {
//                background(red300)
//            }
//            .conditional(dataMessage.statusScreenType == StatusScreenType.PROCESSING) {
//                background(Color.White)
//            }
//    ) {
//        Spacer(modifier = Modifier.height(30.dp))
//
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(7f)
//        ) {
//            // Outer Circle
//            Box(
//                modifier = Modifier
//                    .size(300.dp)
//                    .conditional(dataMessage.statusScreenType == StatusScreenType.PROCESSING) {
//                        scale(pulseScale)
//                            .alpha(pulseAlpha + 0.1f)
//                    }
//                    .background(
//                        color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(50)
//                    ), contentAlignment = Alignment.Center
//            ) {
//
//                // Middle Circle
//                Box(
//                    modifier = Modifier
//                        .size(if (dataMessage.statusScreenType == StatusScreenType.PROCESSING) 255.dp else 235.dp)
//                        .conditional(dataMessage.statusScreenType == StatusScreenType.PROCESSING) {
//                            scale(pulseScale)
//                                .alpha(pulseAlpha + 0.2f)
//                        }
//                        .background(
//                            color = Color(if (dataMessage.statusScreenType == StatusScreenType.ERROR) 0xFFFFC6C7 else 0xFFA2C3F4).copy(
//                                alpha = 0.5f
//                            ), shape = RoundedCornerShape(50)
//                        ), contentAlignment = Alignment.Center
//                ) {
//
//                    // Inner Circle
//                    Box(
//                        modifier = Modifier
//                            .size(if (dataMessage.statusScreenType == StatusScreenType.PROCESSING) 186.dp else 105.dp)
//                            .conditional(dataMessage.statusScreenType == StatusScreenType.PROCESSING) {
//                                scale(pulseScale)
//                            }
//                            .background(
//                                color = Color(
//                                    if (dataMessage.statusScreenType == StatusScreenType.ERROR) 0xFFCD5557 else 0xFF87AEF2
//                                ), shape = RoundedCornerShape(50)
//                            ), contentAlignment = Alignment.Center
//                    ) {
//                        if (dataMessage.statusScreenType != StatusScreenType.PROCESSING) Icon(
//                            painter = painterResource(if (dataMessage.statusScreenType == StatusScreenType.ERROR) R.drawable.error else R.drawable.check),
//                            contentDescription = "Icon",
//                            tint = Color.White,
//                            modifier = Modifier
//                                .size(if (dataMessage.statusScreenType == StatusScreenType.ERROR) 78.dp else 50.dp)
//                                .background(Color.Transparent)
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(50.dp))
//
////            Text(
////                text = dataMessage.text,
////                fontWeight = FontWeight.Bold,
////                fontSize = 35.sp,
////                color = Color.Black
////            )
//            Text(
//                text = dataMessage.text, style = AppTheme.typography.status
//            )
//        }
//    }
//
//}
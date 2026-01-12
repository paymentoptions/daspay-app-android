package com.paymentoptions.pos.ui.composables.screens.dashboard

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.R
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP


@Composable
fun VerticalArrowLine(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFFA500), // orange
    strokeWidth: Dp = 3.dp,
    arrowSize: Dp = 12.dp
) {
    Canvas(modifier = modifier) {
        val strokePx = strokeWidth.toPx()
        val arrowPx = arrowSize.toPx()

        val centerX = size.width / 2

        // Draw line
        drawLine(
            color = color,
            start = Offset(centerX, 0f),
            end = Offset(centerX, size.height - arrowPx),
            strokeWidth = strokePx
        )

        // Draw arrow head
        val path = Path().apply {
            moveTo(centerX - arrowPx, size.height - arrowPx)
            lineTo(centerX, size.height)
            lineTo(centerX + arrowPx, size.height - arrowPx)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokePx)
        )
    }
}

@Composable
fun NavHighlightItem(
    icon: ImageVector,
    label: String,
    title: String,
    focusIcon: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(title, color = Color(0xFFFFB74D), fontSize = 12.sp, textAlign = TextAlign.Center)
        VerticalArrowLine(
            modifier = Modifier
                .height(if (focusIcon)  90.dp else 120.dp)
                .width(24.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .sizeIn(minWidth = 56.dp, minHeight = if (focusIcon)  86.dp else 56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if(focusIcon){
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color.White,
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFF1976D2), shape = RoundedCornerShape(50))
                            .border(3.dp, Color.White, RoundedCornerShape(50))
                            .padding(6.dp)
                    )
                    Spacer(modifier = Modifier.height(5.dp))

                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color(0xFF1976D2), // blue tint for icon
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = label,
                    color = Color(0xFF1976D2),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(all = 3.dp)
                )
            }
        }
    }
}

@Composable
fun OverLayScreen(context: Context, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable {
                onDismiss()
                SharedPreferences.saveBoolean(context, "dashboard_overlay_shown", true)
            },
        contentAlignment = Alignment.TopCenter
    ) {
        // Main overlay content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(220.dp))
            // Logo area spacing

                FilledButton(
                    text = "View Insights",
                    fontSize = 22.sp,
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                        .width(220.dp)
                        .height(50.dp)
                        .scale(0.8f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(width = 5.dp, color = Color.White, shape = RoundedCornerShape(16.dp))
                )


            // Sales summary arrow and label
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row( verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        painter =  painterResource(R.drawable.arrow_sales),
                        contentDescription = null,
                        tint =  Color(0xFFFFA500),
                        modifier = Modifier.size(58.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Sales\nSummary",
                        color = Color(0xFFFFB74D),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
            // Discover text
            Text(
                text = "Discover our\nkey features",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "(tap anywhere to dismiss)",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(60.dp))
            Spacer(modifier = Modifier.weight(1f))
            // Bottom navigation highlights
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 0.dp, 0.dp, 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NavHighlightItem(
                    icon = Icons.Outlined.Dashboard,
                    label = "Home",
                    title = "Main\nScreen"
                )
                NavHighlightItem(
                    icon = Icons.Outlined.Fastfood,
                    label = "Food Menu",
                    title = "Menu\nItems"
                )
                NavHighlightItem(
                    icon = Icons.Filled.Add,
                    label = "Receive Money",
                    title = "Make\nPayment",
                    focusIcon = true
                )
                NavHighlightItem(
                    icon = Icons.Outlined.MoneyOff,
                    label = "Refund",
                    title = "Refund\nAlerts"
                )
                NavHighlightItem(
                    icon = Icons.Outlined.MoreHoriz,
                    label = "More",
                    title = "Extra\nOptions"
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

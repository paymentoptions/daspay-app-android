package com.paymentoptions.pos.ui.composables.screens.settlement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.R
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.containerBackgroundGradientBrush
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary600
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.modifiers.conditional
import com.paymentoptions.pos.utils.modifiers.noRippleClickable

val keypad = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf("00", "0", "←"),
)

@Composable
fun BottomSectionContent(
    navController: NavController,
    enableScrolling: Boolean = false,
) {
    rememberScrollState()
    var showCurrent by remember { mutableStateOf(true) }
    var showDetails by remember { mutableStateOf(false) }
    var loader by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val currency = "HKD"

    LaunchedEffect(showDetails) {
        scrollState.scrollTo(if (showDetails) scrollState.maxValue else 0)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
            .conditional(enableScrolling) {
                verticalScroll(state = scrollState)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = "Settlement",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = primary900,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(iconBackgroundColor),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            Text(
                text = "Current",
                modifier = Modifier
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (showCurrent) Color.White.copy(alpha = 0.9f) else Color.Transparent)
                    .padding(10.dp)
                    .weight(1f)
                    .noRippleClickable(enabled = !showCurrent) {
                        showCurrent = true
                    },
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = primary600
            )
            Text(
                text = "Settled Batch",
                modifier = Modifier
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (!showCurrent) Color.White.copy(alpha = 0.9f) else Color.Transparent)
                    .padding(10.dp)
                    .weight(1f)
                    .noRippleClickable(enabled = showCurrent) {
                        showCurrent = false
                    },
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = primary600
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = containerBackgroundGradientBrush, shape = RoundedCornerShape(20.dp)
                )
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Wait for Settle",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0543B6),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
            )

            HorizontalDivider(
                color = Color.LightGray.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "MID", style = AppTheme.typography.footnote.copy(
                            fontWeight = FontWeight.Normal, fontSize = 14.sp
                        )
                    )

                    Text(
                        "******7890",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "TID", style = AppTheme.typography.footnote.copy(
                            fontWeight = FontWeight.Normal, fontSize = 14.sp
                        )
                    )

                    Text(
                        "****5678",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Batch", style = AppTheme.typography.footnote.copy(
                            fontWeight = FontWeight.Normal, fontSize = 14.sp
                        )
                    )

                    Text(
                        "000017",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Currency", style = AppTheme.typography.footnote.copy(
                            fontWeight = FontWeight.Normal, fontSize = 14.sp
                        )
                    )

                    Text(
                        currency,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Updated", style = AppTheme.typography.footnote.copy(
                            fontWeight = FontWeight.Normal, fontSize = 14.sp
                        )
                    )

                    Text(
                        "2025/08/06 09:37:03 (GMT+4)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )
                }

                HorizontalDivider(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                if (!showCurrent) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Grand Total", style = AppTheme.typography.footnote.copy(
                                fontWeight = FontWeight.Normal, fontSize = 14.sp
                            )
                        )

                        Text(
                            "$100.00",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = primary500
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Refund Total", style = AppTheme.typography.footnote.copy(
                                fontWeight = FontWeight.Normal, fontSize = 14.sp
                            )
                        )

                        Text(
                            "$0.00",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = primary500
                        )
                    }

                    HorizontalDivider(
                        color = Color.LightGray.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )


                }

                if (!showCurrent) {
                    AssistChip(
                        onClick = { showDetails = !showDetails }, label = {
                            Text(
                                text = "View Refund Status",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primary500
                            )
                        }, border = borderThin, colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.Transparent,
                        ), trailingIcon = {
                            Icon(
                                painterResource(R.drawable.down_arrow),
                                contentDescription = "Hint",
                                tint = primary500,
                                modifier = Modifier
                                    .size(AssistChipDefaults.IconSize.minus(5.dp))
                                    .conditional(showDetails) { rotate(180f) })
                        })

                }

                if (showCurrent || showDetails) {
                    //Charge Summary
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Charge Summary",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = primary500
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "SALE", style = AppTheme.typography.footnote.copy(
                                    fontWeight = FontWeight.Normal, fontSize = 14.sp
                                ), modifier = Modifier.weight(1f)
                            )

                            Text(
                                "13",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = primary500,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                "$189.50",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = primary500,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "CAPTURE", style = AppTheme.typography.footnote.copy(
                                    fontWeight = FontWeight.Normal, fontSize = 14.sp
                                ), modifier = Modifier.weight(1f)
                            )

                            Text(
                                "0",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = primary500,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                "$0.00",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = primary500,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "REFUND", style = AppTheme.typography.footnote.copy(
                                    fontWeight = FontWeight.Normal, fontSize = 14.sp
                                ), modifier = Modifier.weight(1f)
                            )

                            Text(
                                "0",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = primary500,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                "$0.00",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = primary500,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        FilledButton(
            text = "Settle All",
            disabled = false,
            isLoading = loader,
            modifier = Modifier
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .fillMaxWidth()
                .height(59.dp),
            onClick = {})
    }
}

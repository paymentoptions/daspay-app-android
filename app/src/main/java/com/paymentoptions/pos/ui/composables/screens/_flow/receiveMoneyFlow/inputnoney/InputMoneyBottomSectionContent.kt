package com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow.inputnoney

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.device.getTransactionCurrency
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.inputs.DashedBorderInput
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow.ReceiveMoneyFlowStage
import com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow.formatAmount
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.innerShadow
import com.paymentoptions.pos.ui.theme.noBorder
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary600
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.ui.theme.shadowColor
import com.paymentoptions.pos.utils.modifiers.conditional
import com.paymentoptions.pos.utils.modifiers.innerShadow
import com.paymentoptions.pos.utils.modifiers.noRippleClickable

val keypad = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf("00", "0", "←"),
)

@Composable
fun InputMoneyBottomSectionContent(
    navController: NavController,
    enableScrolling: Boolean = false,
    amountToCharge: String,
    updateAmountToCharge: (String) -> Unit,
    updateNoteState: (String) -> Unit = {},
    updateFlowStage: (ReceiveMoneyFlowStage) -> Unit = {},
) {
    val scrollState = rememberScrollState()
    var showReceiveMoney by remember { mutableStateOf(true) }
    var paymentLoader by remember { mutableStateOf(false) }
    val noteState = rememberTextFieldState()
    val context = LocalContext.current
    val currency = getTransactionCurrency(context)
    var lastClicked by remember { mutableStateOf<String?>(null) }

    val formattedAmount = formatAmount(amountToCharge)

    val onKeyPress: (String) -> Unit = { key ->
        lastClicked = key

        when (key) {
            "←" -> if (amountToCharge.isNotEmpty()) updateAmountToCharge(amountToCharge.dropLast(1))
            else -> if (amountToCharge.length < 9) updateAmountToCharge(amountToCharge + key)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .padding(bottom = 0.dp)
                .border(width = 2.dp, color = shadowColor, shape = RoundedCornerShape(5.dp))
                .innerShadow(
                    color = innerShadow,
                    blur = 8.dp,
                    spread = 5.dp,
                    cornersRadius = 8.dp,
                    offsetX = 0.dp,
                    offsetY = 0.dp
                )
                .background(iconBackgroundColor),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Receive Money",
                modifier = Modifier
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(
                        width = 1.5.dp,
                        // Border only shows when this tab is selected
                        color = if (showReceiveMoney) shadowColor else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(if (showReceiveMoney) Color.White.copy(alpha = 0.9f) else Color.Transparent)
                    .padding(10.dp)
                    .weight(1f)
                    .noRippleClickable(enabled = !showReceiveMoney) {
                        showReceiveMoney = true
                    },
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = primary600
            )
            Text(
                text = "Pre-Authorize",
                modifier = Modifier
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(
                        width = 1.5.dp,
                        color = if (!showReceiveMoney) shadowColor else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(if (!showReceiveMoney) Color.White.copy(alpha = 0.9f) else Color.Transparent)
                    .padding(10.dp)
                    .weight(1f)
                    .noRippleClickable(enabled = showReceiveMoney) {
                        showReceiveMoney = false
                    },
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = primary600
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (showReceiveMoney) "Receive Money" else "Pre-Authorize",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = primary900,
            )

            Spacer(modifier = Modifier.height(8.dp))
            CurrencyText(currency = currency, amount = formattedAmount, fontWeight = FontWeight(990))
        }

        Spacer(modifier = Modifier.height(16.dp))

        DashedBorderInput(
            state = noteState,
            modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            placeholder = "Add a note (optional)",
            maxLength = 50,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .fillMaxWidth()
                .background(Color.White)
                .conditional(enableScrolling) {
                    weight(1f).verticalScroll(scrollState)
                }) {

            keypad.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
//                                .border(
//                                    2.dp, primary100.copy(alpha = 0.2f), RoundedCornerShape(8.dp)
//                                )
//                                .background(Color.White, RoundedCornerShape(8.dp))
                                .innerShadow(
                                    color = innerShadow,
                                    blur = if (lastClicked === key) 40.dp else 1.dp,
                                    spread = if (lastClicked === key) 5.dp else 1.dp,
                                    cornersRadius = 8.dp,
                                    offsetX = 0.dp,
                                    offsetY = 0.dp
                                )
                                .clickable { onKeyPress(key) }, contentAlignment = Alignment.Center
                        ) {
                            if (key == "←") Icon(
                                Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = primary500,
                            )
                            else Text(
                                key,
                                color = primary500,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {
                SuggestionChip(
                    border = noBorder,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color(0xFFDFF0FC)
                    ),
                    onClick = { updateAmountToCharge("1000") },
                    label = {
                        CurrencyText(
                            currency = currency,
                            amount = "10",
                            fontSize = 16.sp,
                            fontWeight = FontWeight(980),
                            addSpaceAfterCurrency = true
                        )
                    })
            }

            items(20) {
                SuggestionChip(
                    border = noBorder,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color(0xFFDFF0FC)
                    ),
                    onClick = { updateAmountToCharge("${(it + 1) * 50}00") },
                    label = {
                        CurrencyText(
                            currency = currency,
                            amount = "${(it + 1) * 50}",
                            fontSize = 16.sp,
                            addSpaceAfterCurrency = true,
                            fontWeight = FontWeight(980)
                        )
                    })
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        FilledButton(
            text = "Charge",
            disabled = amountToCharge.isEmpty() || amountToCharge.toLong() <= 0,
            isLoading = paymentLoader,
            modifier = Modifier
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .fillMaxWidth()
                .height(59.dp),
            onClick = {
                updateNoteState(noteState.text.toString())
                updateFlowStage(ReceiveMoneyFlowStage.CHARGE_MONEY)
            })
    }
}

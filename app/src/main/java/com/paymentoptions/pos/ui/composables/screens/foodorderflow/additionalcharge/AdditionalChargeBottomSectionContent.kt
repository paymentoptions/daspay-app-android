package com.paymentoptions.pos.ui.composables.screens.foodorderflow.additionalcharge

import android.app.Activity
import android.widget.Toast
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.inputs.DashedBorderInput
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.composables.screens.foodorderflow.Cart
import com.paymentoptions.pos.ui.composables.screens.foodorderflow.FlowStage
import com.paymentoptions.pos.ui.theme.noBorder
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.modifiers.conditional

@Composable
fun AdditionalChargeBottomSectionContent(
    navController: NavController,
    enableScrolling: Boolean = false,
    cartState: Cart,
    updateCartState: (Cart) -> Unit,
    updateFlowStage: (FlowStage) -> Unit,
) {
    val scrollState = rememberScrollState()
    var rawInput by remember { mutableStateOf("") }

    fun formatAmount(input: String): String {
        if (input.isEmpty()) return "0.00"
        val cents = input.toLong()
        val dollars = cents / 100
        val centPortion = (cents % 100).toString().padStart(2, '0')
        return "$dollars.$centPortion"
    }

    val formattedAmount = formatAmount(rawInput)

    val onKeyPress: (String) -> Unit = { key ->
        when (key) {
            "←" -> {
                if (rawInput.isNotEmpty()) rawInput = rawInput.dropLast(1)
            }

            else -> {
                // Avoid too many digits
                if (rawInput.length < 9) rawInput += key
            }
        }
    }

    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("00", "0", "←"),
    )
    val context = LocalContext.current
    context as? Activity
    rememberCoroutineScope()

    val authDetails = SharedPreferences.getAuthDetails(context)

    val noteState = rememberTextFieldState()

    if (authDetails == null) {
        Toast.makeText(context, "Token invalid! Please login again.", Toast.LENGTH_LONG).show()
        navController.navigate(Screens.SignIn.route) {
            popUpTo(0) { inclusive = true }
        }
        return
    }

    val currency = "HKD"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Additional Charge",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = primary900,
            )

            Spacer(modifier = Modifier.height(8.dp))
            CurrencyText(currency = currency, amount = formattedAmount)
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

            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp)
                                .border(
                                    2.dp, primary100.copy(alpha = 0.2f), RoundedCornerShape(8.dp)
                                )
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .clickable { onKeyPress(key) }, contentAlignment = Alignment.Center
                        ) {
                            if (key == "←") {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "Back",
                                    tint = primary500,
                                )
                            } else {
                                Text(
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
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {
                SuggestionChip(
                    border = noBorder,
                    colors = SuggestionChipDefaults.suggestionChipColors()
                        .copy(containerColor = primary100.copy(alpha = 0.15f)),
                    onClick = { rawInput = "1000" },
                    label = {
                        CurrencyText(currency = currency, amount = "10.00", fontSize = 16.sp)
                    })
            }

            items(20) {
                SuggestionChip(
                    border = noBorder,
                    colors = SuggestionChipDefaults.suggestionChipColors()
                        .copy(containerColor = primary100.copy(alpha = 0.15f)),
                    onClick = { rawInput = "${(it + 1) * 50}00" },
                    label = {
                        CurrencyText(
                            currency = currency, amount = "${(it + 1) * 50}", fontSize = 16.sp
                        )
                    })
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        FilledButton(
            text = "Add",
            disabled = rawInput.isEmpty() || rawInput.toLong() <= 0,
            modifier = Modifier
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .fillMaxWidth()
                .height(59.dp),
            onClick = {
                updateCartState(
                    cartState.copy(
                        additionalAmountNote = noteState.text.toString(),
                        additionalCharge = rawInput.toFloat().div(100)
                    )
                )
                updateFlowStage(FlowStage.REVIEW_CART)
            })
    }
}
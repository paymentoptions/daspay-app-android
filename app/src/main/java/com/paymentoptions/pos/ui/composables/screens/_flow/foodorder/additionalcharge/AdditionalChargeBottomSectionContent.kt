package com.paymentoptions.pos.ui.composables.screens._flow.foodorder.additionalcharge

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
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
import com.paymentoptions.pos.ui.composables._components.screentitle.ScreenTitleWithCloseButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.Cart
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.FoodOrderFlowStage
import com.paymentoptions.pos.ui.composables.screens._flow.receivemoney.formatAmount
import com.paymentoptions.pos.ui.theme.innerShadow
import com.paymentoptions.pos.ui.theme.noBorder
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.modifiers.conditional
import com.paymentoptions.pos.utils.modifiers.innerShadow

@Composable
fun AdditionalChargeBottomSectionContent(
    navController: NavController,
    enableScrolling: Boolean = false,
    cartState: Cart,
    updateCartSate: (Cart) -> Unit,
    updateFlowStage: (FoodOrderFlowStage) -> Unit,
) {
    val scrollState = rememberScrollState()
    var lastClicked by remember { mutableStateOf<String?>(null) }

    var rawInput by remember {
        mutableStateOf(
            if (cartState.additionalCharge == 0f) "" else cartState.additionalCharge.times(100)
                .toInt().toString()
        )
    }

    val formattedAmount = formatAmount(rawInput)

    val onKeyPress: (String) -> Unit = { key ->
        lastClicked = key

        when (key) {
            "←" -> if (rawInput.isNotEmpty()) rawInput = rawInput.dropLast(1)
            else -> if (rawInput.length < 9) rawInput += key
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        ScreenTitleWithCloseButton(
            navController,
            title = "",
            onClose = { updateFlowStage(FoodOrderFlowStage.REVIEW_CART) },
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
        )

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
                                .height(60.dp)
                                .innerShadow(
                                    color = innerShadow,
                                    blur = if (lastClicked === key) 10.dp else 4.dp,
                                    spread = if (lastClicked === key) 3.dp else 0.dp,
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
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color(0xFFDFF0FC)
                    ),
                    onClick = { rawInput = "1000" },
                    label = {
                        CurrencyText(
                            currency = currency,
                            amount = "10",
                            fontSize = 16.sp,
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
                    onClick = { rawInput = "${(it + 1) * 50}00" },
                    label = {
                        CurrencyText(
                            currency = currency,
                            amount = "${(it + 1) * 50}",
                            fontSize = 16.sp,
                            addSpaceAfterCurrency = true
                        )
                    })
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        FilledButton(
            text = "Add",
            modifier = Modifier
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .fillMaxWidth()
                .height(59.dp),
            onClick = {
                cartState.additionalAmountNote = noteState.text.toString()
                cartState.additionalCharge =
                    if (rawInput.isEmpty()) 0.0f else rawInput.toFloat().div(100)
                updateCartSate(cartState)
                updateFlowStage(FoodOrderFlowStage.REVIEW_CART)
            })
    }
}
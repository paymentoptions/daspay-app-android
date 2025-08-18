package com.paymentoptions.pos.ui.composables.screens._flow.foodorder.reviewcart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.ZigZagContainer
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.Cart
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.FoodItem
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.FoodOrderFlowStage
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.foodmenu.FoodSummaryForReview
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.foodmenu.ToastData
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.containerBackgroundGradientBrush
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.formatToPrecisionString
import com.paymentoptions.pos.utils.modifiers.conditional
import com.paymentoptions.pos.utils.modifiers.dashedBorder

@Composable
fun ReviewCartBottomSectionContent(
    navController: NavController,
    enableScrolling: Boolean = false,
    cartState: Cart,
    updateCartSate: (Cart) -> Unit,
    updateFlowStage: (FoodOrderFlowStage) -> Unit,
    createToast: (ToastData) -> Unit,
    setShowToast: (Boolean) -> Unit,
) {
    LocalContext.current
    val scrollState = rememberScrollState()
    val currency = "HKD"
    var longClickedFoodItem by remember { mutableStateOf<FoodItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Review Order", style = AppTheme.typography.titleNormal
            )

            AssistChip(
                onClick = { updateFlowStage(FoodOrderFlowStage.MENU) }, label = {
                    Text(
                        "Add More Items",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primary500
                    )
                }, border = borderThin, colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color.Transparent,
                ), leadingIcon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Hint",
                        tint = primary500,
                        modifier = Modifier.size(AssistChipDefaults.IconSize)
                    )
                })
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .conditional(enableScrolling) {
                    weight(1f).verticalScroll(scrollState)
                }, verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            cartState.getFoodItemsForReview().forEach { foodItem ->
                FoodSummaryForReview(
                    foodItem,
                    longClickedFoodItem,
                    cartState,
                    updateLongClickedFoodItem = {
                        longClickedFoodItem = if (longClickedFoodItem === it) null else it
                    },
                    updateCartSate = { updateCartSate(cartState) },
                    createToast = { createToast(it) },
                    setShowToast = { setShowToast(it) })
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            verticalArrangement = Arrangement.spacedBy(8.dp)

        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Additional Charge (Optional)",
                    style = AppTheme.typography.titleNormal.copy(
                        color = primary900, fontSize = 14.sp, fontWeight = FontWeight.Bold
                    )
                )

                if (cartState.additionalCharge != 0.0f) AssistChip(
                    onClick = { updateFlowStage(FoodOrderFlowStage.ADDITIONAL_CHARGE) },
                    label = {
                        Text(
                            "Change Amount",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primary500
                        )
                    },
                    border = borderThin,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color.Transparent,
                    ),
                )
            }

            Column(
                modifier = Modifier
                    .height(40.dp)
                    .dashedBorder(color = Color.LightGray, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                    .clickable {
                        updateFlowStage(FoodOrderFlowStage.ADDITIONAL_CHARGE)
                    }, verticalArrangement = Arrangement.Center
            ) {

                if (cartState.additionalCharge.toFloat() == 0.0f) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Enter Amount",
                            style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.SemiBold)
                        )

                        CurrencyText(
                            currency,
                            cartState.additionalCharge.formatToPrecisionString(),
                            fontSize = 12.sp
                        )
                    }
                } else {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Amount",
                            style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.SemiBold)
                        )

                        CurrencyText(
                            currency,
                            "",
                            fontSize = 12.sp,
                            textAlign = TextAlign.End,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Note: " + cartState.additionalAmountNote,
                            style = AppTheme.typography.footnote.copy(
                                fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic
                            )
                        )

                        CurrencyText(
                            "",
                            cartState.additionalCharge.toString(),
                            fontSize = 12.sp,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }

        ZigZagContainer {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = containerBackgroundGradientBrush)
                    .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Summary",
                    style = AppTheme.typography.titleNormal.copy(color = primary900)
                )

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(), color = Color.LightGray.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Item total", style = AppTheme.typography.footnote.copy(
                            fontSize = 14.sp, fontWeight = FontWeight.Normal
                        )
                    )

                    CurrencyText(
                        currency = currency,
                        amount = "+" + cartState.itemTotal.formatToPrecisionString(),
                        fontSize = 14.sp,
                        color = primary500
                    )

                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Service Charge (${cartState.serviceChargePercentage}%)",
                        style = AppTheme.typography.footnote.copy(
                            fontSize = 14.sp, fontWeight = FontWeight.Normal
                        )
                    )


                    CurrencyText(
                        currency = currency,
                        amount = "+" + cartState.calculateServiceCharge().formatToPrecisionString(),
                        fontSize = 14.sp,
                        color = primary500
                    )

                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Additional Charge", style = AppTheme.typography.footnote.copy(
                            fontSize = 14.sp, fontWeight = FontWeight.Normal
                        )
                    )

                    CurrencyText(
                        currency = currency,
                        amount = "+" + cartState.additionalCharge.formatToPrecisionString(),
                        fontSize = 14.sp,
                        color = primary500,
                    )

                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "GST (${cartState.gstPercentage}%)",
                        style = AppTheme.typography.footnote.copy(
                            fontSize = 14.sp, fontWeight = FontWeight.Normal
                        )
                    )

                    CurrencyText(
                        currency = currency,
                        amount = "+" + cartState.calculateGstCharge().formatToPrecisionString(),
                        fontSize = 14.sp,
                        color = primary500
                    )

                }

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(), color = Color.LightGray.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Grand Total", style = AppTheme.typography.footnote.copy(
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = primary500
                        )
                    )

                    CurrencyText(
                        currency = currency,
                        amount = "+" + cartState.calculateGrandTotal().formatToPrecisionString(),
                        fontSize = 16.sp,
                        color = primary500,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        FilledButton(
            disabled = cartState.itemQuantity == 0,
            text = "Receive Money",
            onClick = { updateFlowStage(FoodOrderFlowStage.CHARGE_MONEY) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .height(59.dp)
        )
    }
}
package com.paymentoptions.pos.ui.composables.screens.foodorderflow.reviewcart

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.screens.foodorderflow.Cart
import com.paymentoptions.pos.ui.composables.screens.foodorderflow.FlowStage
import com.paymentoptions.pos.ui.composables.screens.foodorderflow.foodmenu.FoodSummary
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.modifiers.conditional
import com.paymentoptions.pos.utils.modifiers.dashedBorder

fun calculateGrandTotal(cart: Cart): Float {
    val serviceCharge = cart.itemTotal.times(cart.serviceChargePercentage.div(100))
    val gst = cart.itemTotal.times(cart.gstPercentage.div(100))

    return cart.itemTotal.plus(serviceCharge).plus(gst).plus(cart.additionalCharge)
}

@Composable
fun ReviewCartBottomSectionContent(
    navController: NavController,
    enableScrolling: Boolean = false,
    cartState: Cart,
    updateFlowStage: (FlowStage) -> Unit,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val currency = "HKD"

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
                onClick = { updateFlowStage(FlowStage.MENU) }, label = {
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
                .padding(
                    horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
                )
                .conditional(enableScrolling) {
                    weight(1f).verticalScroll(scrollState)
                }, verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            cartState.getFoodItems().forEach { foodItem ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(borderThin, shape = RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    colors = CardDefaults.cardColors().copy(
                        containerColor = Color.Transparent
                    )
                ) {
                    FoodSummary(
                        foodItem,
                        cartState,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .height(40.dp)
                .dashedBorder(
                    color = Color.LightGray, shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .clickable {
                    updateFlowStage(FlowStage.ADDITIONAL_CHARGE)
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

                    CurrencyText(currency, cartState.additionalCharge.toString(), fontSize = 12.sp)
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
                        addSpaceAfterCurrency = false
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

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text(
                text = "Summary", style = AppTheme.typography.titleNormal.copy(color = primary900)
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.2f))
            )

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "item total", style = AppTheme.typography.footnote.copy(
                        fontSize = 14.sp, fontWeight = FontWeight.Normal
                    )
                )

                CurrencyText(
                    currency = currency,
                    amount = "+" + cartState.itemTotal.toString(),
                    fontSize = 14.sp
                )

            }


            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Service Charge (${cartState.serviceChargePercentage}%)",
                    style = AppTheme.typography.footnote.copy(
                        fontSize = 14.sp, fontWeight = FontWeight.Normal
                    )
                )


                CurrencyText(
                    currency = currency, amount = "+" + (cartState.itemTotal.times(
                        cartState.serviceChargePercentage.div(100)
                    ).toString()), fontSize = 14.sp
                )

            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Additional Charge", style = AppTheme.typography.footnote.copy(
                        fontSize = 14.sp, fontWeight = FontWeight.Normal
                    )
                )

                CurrencyText(
                    currency = currency,
                    amount = "+" + cartState.additionalCharge.toString(),
                    fontSize = 14.sp
                )

            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "GST (${cartState.gstPercentage}%)", style = AppTheme.typography.footnote.copy(
                        fontSize = 14.sp, fontWeight = FontWeight.Normal
                    )
                )

                CurrencyText(
                    currency = currency,
                    amount = "+" + cartState.itemTotal.times(cartState.gstPercentage.div(100))
                        .toString(),
                    fontSize = 14.sp
                )

            }

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.2f))
            )

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Grand Total", style = AppTheme.typography.footnote.copy(
                        fontSize = 14.sp, fontWeight = FontWeight.Normal
                    )
                )

                CurrencyText(
                    currency = currency,
                    amount = "+" + calculateGrandTotal(cartState).toString(),
                    fontSize = 14.sp
                )

            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        FilledButton(
            disabled = cartState.itemQuantity == 0,
            text = "Receive Money",
            onClick = {
                Toast.makeText(context, "Under Development", Toast.LENGTH_SHORT).show()
//                updateFlowStage(FlowStage.PAYMENT)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .height(59.dp)
        )
    }
}
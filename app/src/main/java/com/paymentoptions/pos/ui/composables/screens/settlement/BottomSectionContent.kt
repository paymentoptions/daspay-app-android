package com.paymentoptions.pos.ui.composables.screens.settlement

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.analytics.AnalyticsHelper
import com.paymentoptions.pos.R
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.device.DPStorageManager.getSettlementCurrency
import com.paymentoptions.pos.network.endpoints.settlementList
import com.paymentoptions.pos.network.SettlementRecord
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.innerShadow
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary600
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.ui.theme.shadowColor
import com.paymentoptions.pos.utils.modifiers.conditional
import com.paymentoptions.pos.utils.modifiers.innerShadow
import com.paymentoptions.pos.utils.modifiers.noRippleClickable
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.utils.getDeviceIdentifier

const val SETTLED_BATCH = "settled"
const val PENDING_BATCH = "pending"

@Composable
fun BottomSectionContent(
    navController: NavController,
    enableScrolling: Boolean = false,
) {
    val scrollState = rememberScrollState()
    var showCurrent by remember { mutableStateOf(true) }
    var loader by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currency = getSettlementCurrency()

    // Settlement data states
    var pendingSettlement by remember { mutableStateOf<SettlementRecord?>(null) }
    var settledBatches by remember { mutableStateOf<List<SettlementRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshList by remember { mutableStateOf(false) }

    // Fetch settlement data
    LaunchedEffect(refreshList) {
        isLoading = true
        try {
            val response = settlementList(
                deviceNumber = AppStorage.deviceNumber ?: getDeviceIdentifier(),
                uniqueCode = AppStorage.tokenCode ?: "",
            )
            if (response != null && response.success) {
                val allRecords = response.data.records

                // Separate pending and settled records
                isLoading = false
                pendingSettlement = allRecords.firstOrNull { it.SettleStatus == PENDING_BATCH }
                settledBatches = allRecords.filter { it.SettleStatus == SETTLED_BATCH }
                  //  .sortedByDescending { it.SettledAt ?: it.UpdatedAt ?: it.CreatedAt }
            }
        } catch (e: Exception) {
            // Handle error
            AppLogger.error("settlementList error: $e")
        } finally {
            isLoading = false
        }
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

            // Toggle between Current and Settled Batch
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
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
                    text = "Current",
                    modifier = Modifier
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(
                            width = 1.dp,
                            color = if (showCurrent) Color(0xFFDCEAFE) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
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
                        .border(
                            width = 1.dp,
                            color = if (!showCurrent) Color(0xFFDCEAFE) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
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

            // Show current pending settlement
            if (showCurrent) {
                if (isLoading) {
                    SettlementCardShimmer()
                } else {
                    if (pendingSettlement != null) {
                        SettlementCard(
                            settlement = pendingSettlement!!,
                            currency = currency,
                            isPending = true
                        )
                    } else {
                        Text(
                            text = "No pending settlement",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    }
                }
            } else {
                // Show settled batches
                if (isLoading) {
                    SettlementCardShimmer()
                    Spacer(modifier = Modifier.height(12.dp))
                    SettlementCardShimmer()
                } else {
                    if (settledBatches.isNotEmpty()) {
                        settledBatches.forEach { settlement ->
                            SettlementCard(
                                settlement = settlement,
                                currency = currency,
                                isPending = false
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    } else {
                        Text(
                            text = "No settled batches",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Show Settle All button only for admin and when showing current
            if (DPStorageManager.isAdmin() && showCurrent && pendingSettlement != null) {
                Spacer(modifier = Modifier.height(20.dp))
                FilledButton(
                    text = "Settle All",
                    disabled = false,
                    isLoading = loader,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(59.dp),
                    onClick = {
                        AnalyticsHelper.trackCriticalButtonClick(buttonName = "Settle All", screenName = "Settlement")
                        AnalyticsHelper.trackSettlementSubmitted(batchId = pendingSettlement!!.BatchID)
                        isLoading = true
                        navController.navigate(Screens.SettlementAction.createRoute(pendingSettlement!!.BatchID))
                    }
                )
            }

    }
}

@Composable
fun SettlementCard(
    settlement: SettlementRecord,
    currency: String,
    isPending: Boolean
) {
    var showDetails by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Get device info
    val deviceConfig = DPStorageManager.getDeviceConfiguration()
    val mid = deviceConfig?.data?.deviceInfo?.DASMID ?: "00000000"
    val tid = deviceConfig?.data?.deviceInfo?.DeviceNumber?.takeLast(4) ?: "0000"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFE6F6FF), shape = RoundedCornerShape(20.dp)
            )
            .border(
                border = BorderStroke(width = 1.dp, color = Color(0xFFCBEBFF)),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(bottom = 24.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isPending) "Wait for Settle" else "Settled Batch",
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
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP.plus(5.dp)),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SettlementDetailRow("MID", mid)
            SettlementDetailRow("TID", tid)
            SettlementDetailRow("Batch", settlement.BatchNo)
            SettlementDetailRow("Currency", currency)
            SettlementDetailRow("Updated", formatDate(settlement.UpdatedAt ?: settlement.CreatedAt))

            if (!isPending) {
                HorizontalDivider(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Grand Total and Refund Total
                val grandTotal = settlement.SaleAmount + settlement.CaptureAmount
                val refundTotal = settlement.RefundAmount

                SettlementDetailRow("Grand Total", "$${String.format(Locale.US, "%.2f", grandTotal)}")
                SettlementDetailRow("Refund Total", "$${String.format(Locale.US, "%.2f", refundTotal)}")

                HorizontalDivider(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Details toggle button
                AssistChip(
                    onClick = { showDetails = !showDetails },
                    label = {
                        Text(
                            text = if (showDetails) "Hide Details" else "View Details",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primary500
                        )
                    },
                    border = borderThin,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color.Transparent,
                    ),
                    trailingIcon = {
                        Icon(
                            painterResource(R.drawable.down_arrow),
                            contentDescription = "Toggle Details",
                            tint = primary500,
                            modifier = Modifier
                                .size(AssistChipDefaults.IconSize.minus(5.dp))
                                .conditional(showDetails) { rotate(180f) }
                        )
                    }
                )
            }

            if (isPending || showDetails) {
                // Summary Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Summary",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primary500
                    )

                    // SALE
                    SummaryRow(
                        label = "SALE",
                        count = settlement.Sale.toInt(),
                        amount = settlement.SaleAmount
                    )

//                    // CAPTURE
//                    SummaryRow(
//                        label = "CAPTURE",
//                        count = settlement.Capture.toInt(),
//                        amount = settlement.CaptureAmount
//                    )

                    // REFUND
                    SummaryRow(
                        label = "REFUND",
                        count = settlement.Refund.toInt(),
                        amount = settlement.RefundAmount
                    )

                    // VOID
                    SummaryRow(
                        label = "VOID",
                        count = settlement.Voided.toInt(),
                        amount = settlement.VoidedAmount
                    )
                }
            }
        }
    }
}

@Composable
fun SettlementDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = AppTheme.typography.footnote.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp
            )
        )

        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = primary500
        )
    }
}

@Composable
fun SummaryRow(label: String, count: Int, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = AppTheme.typography.footnote.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp
            ),
            modifier = Modifier.weight(1f)
        )

        Text(
            count.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = primary500,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Text(
            "$${String.format(Locale.US, "%.2f", amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = primary500,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)

        val outputFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss (z)", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (_: Exception) {
        dateString
    }
}

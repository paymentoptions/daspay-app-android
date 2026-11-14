package com.paymentoptions.pos.ui.composables._components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.network.DaspayProductDetailData
import com.paymentoptions.pos.network.MerchantSetting
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.utils.formatToPrecisionString

/** The line names the food-order pay-by-link request uses (see getPayByLinkRequest). */
private const val SERVICE_CHARGE_LINE = "Total Service Charge"
private const val ADDITIONAL_CHARGE_PREFIX = "Additional Charge"

// TotalPrice may arrive formatted with thousands separators.
private fun DaspayProductDetailData.totalAmount(): Double =
    TotalPrice.replace(",", "").toDoubleOrNull() ?: 0.0

/**
 * Item total, service charge, additional charge and tax lines for a food order, taken from the
 * payment's DaspayProductDetails. Shows nothing (not even the divider) when there are no details.
 */
@Composable
fun ReceiptBreakdown(
    productDetails: List<DaspayProductDetailData>?,
    merchantSetting: MerchantSetting?,
    horizontalPadding: Dp = 0.dp,
) {
    if (productDetails.isNullOrEmpty()) return

    val serviceChargeItem = productDetails.find { it.Name == SERVICE_CHARGE_LINE }
    val additionalChargeItem = productDetails.find { it.Name.startsWith(ADDITIONAL_CHARGE_PREFIX) }
    val taxName = merchantSetting?.TaxName
    val taxItem = productDetails.find {
        it.Name.contains("GST", ignoreCase = true) ||
            it.Name.contains("JCT", ignoreCase = true) ||
            (taxName != null && it.Name.contains(taxName, ignoreCase = true))
    }
    val itemsTotal = productDetails
        .filter {
            it != serviceChargeItem && it != additionalChargeItem && it != taxItem &&
                !it.Name.contains("TAX", ignoreCase = true) &&
                !it.Name.contains("REGISTRATION", ignoreCase = true)
        }
        .sumOf { it.totalAmount() }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (itemsTotal > 0) {
            ReceiptBreakdownRow(label = "Total Item(s)", amount = itemsTotal.formatToPrecisionString())
        }
        if (serviceChargeItem != null && serviceChargeItem.totalAmount() > 0) {
            ReceiptBreakdownRow(label = SERVICE_CHARGE_LINE, amount = serviceChargeItem.TotalPrice)
        }
        if (additionalChargeItem != null && additionalChargeItem.totalAmount() > 0) {
            ReceiptBreakdownRow(label = ADDITIONAL_CHARGE_PREFIX, amount = additionalChargeItem.TotalPrice)
        }
        if (taxItem != null) {
            ReceiptBreakdownRow(label = taxItem.Name, amount = taxItem.TotalPrice)
        }
    }

    HorizontalDivider(
        modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
        color = Color.LightGray.copy(alpha = 0.2f),
    )
}

@Composable
private fun ReceiptBreakdownRow(label: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = primary500)
        Text(text = "+$amount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primary500)
    }
}

/** The merchant's tax registration number, shown only when its catalog (and so tax) is enabled. */
fun MerchantSetting?.taxRegistrationNumberToShow(): String? =
    this?.takeIf { it.CatalogEnabled == true }?.TaxRegistrationNumber?.takeIf { it.isNotBlank() }

/** "TAX REGISTRATION NO." row in the receipt's label/value style. */
@Composable
fun TaxRegistrationRow(merchantSetting: MerchantSetting?) {
    val number = merchantSetting.taxRegistrationNumberToShow() ?: return
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            "TAX REGISTRATION NO.",
            style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal, fontSize = 14.sp),
        )
        Text(number, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500)
    }
}

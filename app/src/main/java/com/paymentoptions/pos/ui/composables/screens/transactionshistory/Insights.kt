package com.paymentoptions.pos.ui.composables.screens.transactionshistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarData
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.ui.composables._components.NoData
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.green500
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import java.time.OffsetDateTime
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun Insights(
    transactions: Array<TransactionListDataRecord>?,
    currency: String,
    updateReceivalAmount: (Float) -> Unit,
) {
//    val scrollState = rememberScrollState()
    var saleTransactionCount = 0
    var earningAmount = 0.0f
    var refundTransactionCount = 0
    var refundAmount = 0.0f
    var chartMaxValue = 0.0f
    var chartMinValue = 0.0f
    var barData: MutableList<BarData> = mutableListOf()

    if (transactions == null || transactions.isEmpty()) NoData(text = "No transactions found")
    else Column(
        modifier = Modifier
            .fillMaxWidth()
//            .verticalScroll(scrollState)
        ,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        transactions.forEachIndexed { index, transaction ->

            val amount = transaction.amount.toFloat()
            val date = OffsetDateTime.parse(transaction.Date).toLocalDateTime()

            if (amount > chartMaxValue) chartMaxValue = amount
            if (amount < chartMinValue) chartMinValue = amount

            barData.add(
                BarData(
                    point = Point(x = index.toFloat(), y = transaction.amount.toFloat()),
                    color = if (transaction.TransactionType == "REFUND") Color.Red.copy(alpha = 0.5f) else Color.Green.copy(
                        alpha = 0.5f
                    ),
                    label = "${date.dayOfMonth} ${date.month}",
                    gradientColorList = listOf(Color.Blue, Color.Yellow, Color.Green),
                    description = if (transaction.TransactionType == "REFUND") "Refund" else "Earning",
                )
            )

            when (transaction.TransactionType) {
                "PURCHASE" -> {
                    saleTransactionCount++
                    earningAmount += amount
                }

                "REFUND" -> {
                    refundTransactionCount++
                    refundAmount += amount
                }
            }
        }

        updateReceivalAmount(earningAmount)

        //Bar Graph
        Row(
            modifier = Modifier.fillMaxWidth()

        ) {

            val xAxisData =
                AxisData.Builder().axisStepSize(2.dp).steps(barData.size - 1).bottomPadding(0.dp)
                    .axisLabelColor(Color.LightGray).axisLineThickness(0.dp).axisLabelFontSize(8.sp)
                    .axisLabelAngle(0f).axisLineColor(Color.White).labelAndAxisLinePadding(0.dp)
                    .labelData { index -> barData[index].label }.build()

            val yAxisData =
                AxisData.Builder().axisStepSize(2.dp).steps(5).axisOffset(30.dp).endPadding(0.dp)
                    .axisLabelColor(Color.LightGray).axisLineThickness(0.dp).axisLabelFontSize(8.sp)
                    .axisLabelAngle(0f).axisLineColor(Color.White).labelAndAxisLinePadding(0.dp)
                    .backgroundColor(Color.White).labelData { index ->
                        "${currency} ${(index * (chartMaxValue / (barData.size - 1))).roundToInt()}"
                    }.build()

            val barChartData = BarChartData(
                chartData = barData, xAxisData = xAxisData, yAxisData = yAxisData,
//                showYAxis = false,
//                showXAxis = false,
                paddingTop = 0.dp, paddingEnd = 0.dp
            )

            BarChart(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .background(Color.White),
                barChartData = barChartData
            )
        }

        //Stats
        Column(
            modifier = Modifier
                .fillMaxWidth()
//                .background(primary100.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFE6F6FF), Color.White),
                    ), shape = RoundedCornerShape(8.dp)
                )
                .padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            //Earnings
            Column(
                modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Transactions # $saleTransactionCount",
                        style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal)
                    )

                    Text(
                        currency,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = green500.copy(alpha = 0.5f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Earnings",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )

                    Text(
                        "+ ${earningAmount.absoluteValue}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = green500
                    )
                }
            }

            //Refunds
            Column(
                modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Transactions # $refundTransactionCount",
                        style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal)
                    )

                    Text(
                        currency,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red.copy(alpha = 0.5f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Refunds",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )

                    Text(
                        "- ${refundAmount.absoluteValue}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.background(Color.White.copy(alpha = 0.2f)))

            //All Earnings
            Column(
                modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        "All Transactions # ${transactions.size}",
                        style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal)
                    )

                    Text(
                        currency,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary900.copy(alpha = 0.5f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    val netEarningAmount = earningAmount - refundAmount
                    Text(
                        "Net Earnings",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )

                    Text(
                        "${if (netEarningAmount > 0) "+" else if (netEarningAmount < 0) "-" else ""} ${netEarningAmount.absoluteValue}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary900
                    )
                }
            }


        }
    }
}
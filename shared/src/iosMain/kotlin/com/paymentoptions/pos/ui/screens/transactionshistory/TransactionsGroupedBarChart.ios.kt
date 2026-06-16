//package com.paymentoptions.pos.ui.screens.transactionshistory
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.SolidColor
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.paymentoptions.pos.network.InsightsResponseDataRecord
//import io.github.koalaplot.core.bar.BarChart
//import io.github.koalaplot.core.bar.DefaultBarChartEntry
//import io.github.koalaplot.core.xygraph.CategoryAxisModel
//import io.github.koalaplot.core.xygraph.XYGraph
//import io.github.koalaplot.core.xygraph.rememberIntLinearAxisModel
//import io.github.koalaplot.core.style.KoalaPlotTheme
//import com.paymentoptions.pos.utils.safeParseDateTime
//import kotlinx.datetime.TimeZone
//import kotlinx.datetime.toLocalDateTime
//
//@Composable
//actual fun TransactionsGroupedBarChart(
//    navController: NavController,
//    transactions: List<InsightsResponseDataRecord>,
//    currency: String,
//    updateReceivalAmount: (Float) -> Unit,
//    startDateMillis: Long,
//    endDateMillis: Long,
//) {
//    val tz = TimeZone.currentSystemDefault()
//
//    val entries = remember(transactions) {
//        val earnings = mutableMapOf<String, Float>()
//        transactions.filter { it.status == "SUCCESSFUL" && it.TransactionType == "PURCHASE" }
//            .forEach {
//                val date = safeParseDateTime(it.TransactionDate ?: "").toLocalDateTime(tz)
//                val key = "${date.dayOfMonth}/${date.monthNumber}"
//                earnings[key] = (earnings[key] ?: 0f) + it.amount
//            }
//
//        earnings.map { (key, value) ->
//            DefaultBarChartEntry(
//                xValue = key,
//                yValue = value
//            )
//        }
//    }
//
//    if (entries.isEmpty()) {
//        Box(Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
//            Text("No data for chart", fontSize = 12.sp, color = Color.Gray)
//        }
//        return
//    }
//
//    val xModel = remember(entries) { CategoryAxisModel(entries.map { it.xValue }) }
//    val yModel = rememberIntLinearAxisModel(0..((entries.maxOfOrNull { it.yValue }?.toInt() ?: 100) + 10))
//
//    KoalaPlotTheme {
//        XYGraph(
//            xAxisModel = xModel,
//            yAxisModel = yModel,
//            modifier = Modifier.fillMaxWidth().height(280.dp).padding(16.dp),
//            xAxisTitle = "Date",
//            yAxisTitle = "Amount ($currency)"
//        ) {
//            BarChart(
//                data = entries,
//                bar = { _, _, _ ->
//                    Box(modifier = Modifier.fillMaxWidth(0.5f).background(SolidColor(Color(0xFF00C9A7))))
//                }
//            )
//        }
//    }
//
//    val totalEarnings = entries.sumOf { it.yValue.toDouble() }.toFloat()
//    updateReceivalAmount(totalEarnings)
//}

package com.paymentoptions.pos.ui.composables.screens.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.R
import com.paymentoptions.pos.device.DPStorageManager.getTransactionCurrency
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.endpoints.insights
import com.paymentoptions.pos.network.InsightsResponseDataRecord
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.ui.composables._components.DateRangePickerModal
import com.paymentoptions.pos.ui.composables._components.ScreenTitleWithCloseButton
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.layout.simple.SimpleLayoutWithNavigation
import com.paymentoptions.pos.ui.screens.transactionshistory.Transactions
import com.paymentoptions.pos.ui.screens.transactionshistory.TransactionsGroupedBarChart
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.innerShadow
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.ui.theme.red300
import com.paymentoptions.pos.utils.getDeviceIdentifier
import com.paymentoptions.pos.utils.modifiers.DashboardStatsShimmer
import com.paymentoptions.pos.utils.modifiers.TransactionListShimmer
import com.paymentoptions.pos.utils.modifiers.conditional
import com.paymentoptions.pos.utils.modifiers.innerShadow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

const val ALL = "All"
const val PAY_BY_LINK = "Pay By Link"

@Composable
fun TransactionFilter(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var receivalAmount: Float by remember { mutableFloatStateOf(0.0f) }
    var currency by remember { mutableStateOf(getTransactionCurrency()) }
    var apiResponseAvailable by remember { mutableStateOf(false) }
    var transactions by remember { mutableStateOf<List<InsightsResponseDataRecord>>(listOf()) }

    var fromDateCustomFilter by remember { mutableStateOf<Long?>(null) }
    var toDateCustomFilter by remember { mutableStateOf<Long?>(null) }

    var receivalForText by remember { mutableStateOf("Receival for the day") }
    var receivalForTimePeriodText by remember { mutableStateOf("Today") }

    var startDate by remember { mutableStateOf(OffsetDateTime.now()) }
    var endDate by remember { mutableStateOf(OffsetDateTime.now()) }

    var dateFilterSelected by remember { mutableStateOf(false) }

    // Show filter inputs
    var showFilterQuery by remember { mutableStateOf(true) }
    val amountState = rememberTextFieldState("")
    val tranxIdState = rememberTextFieldState("")
    var selectedPaymentType by remember { mutableStateOf(ALL) }
    var selectedTranxType by remember { mutableStateOf(ALL) }

    // Date picker for custom date range
    if (dateFilterSelected) {
        if (fromDateCustomFilter == null) DateRangePickerModal(
            title = "Start Date",
            { startDateMillis, endDateMillis ->
                if (startDateMillis == null || endDateMillis == null) {
                    fromDateCustomFilter = null
                    toDateCustomFilter = null
                } else {
                    fromDateCustomFilter = startDateMillis
                    toDateCustomFilter = endDateMillis
                }
            },
            { dateFilterSelected = false })
    }
//    else {
//        fromDateCustomFilter = null
//        toDateCustomFilter = null
//    }

    fun updateReceivalAmount(newAmount: Float) {
        receivalAmount = newAmount
    }

    fun resetFilter() {
        transactions = emptyList()
        showFilterQuery = true
        apiResponseAvailable = false
    }

    fun getTransactionType(transactionType: String): String? {
        if (transactionType == ALL) return null;
        when (transactionType) {
            "SALE" -> return "PURCHASE,AUTHORISATION"
            "REFUND" -> return "REFUND"
            "VOID" -> return "VOIDAUTHORISATION"
        }
        return null
    }

    fun getProductType(productType: String): String? {
        if (productType == ALL) return null;
        when (productType) {
            "Tap" -> return "SOFTPOS"
            "QR" -> return "QR"
            PAY_BY_LINK -> return "PBL"
        }
        return null
    }

    LaunchedEffect(fromDateCustomFilter, toDateCustomFilter) {
        receivalForText = "Receival for the period"
        receivalAmount = 0.0f

        if (fromDateCustomFilter != null && toDateCustomFilter != null) {
            val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.US)

            receivalForTimePeriodText =
                "${simpleDateFormat.format(fromDateCustomFilter)} to ${
                    simpleDateFormat.format(toDateCustomFilter)
                }"

            startDate = OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(fromDateCustomFilter!!),
                ZoneId.systemDefault()
            )

            endDate = OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(toDateCustomFilter!!),
                ZoneId.systemDefault()
            )
        } else receivalForTimePeriodText = "Today"
    }

    if (showFilterQuery) {
        // Show filter query form
        SimpleLayoutWithNavigation(navController) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 200.dp)
            ) {
                // Content area that scrolls
                Box(
                    modifier = Modifier
                        .weight(5f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                            .background(Color.White)
                            .innerShadow(
                                color = innerShadow,
                                blur = 20.dp,
                                spread = 10.dp,
                                cornersRadius = 0.dp,
                                offsetX = 0.dp,
                                offsetY = 0.dp,
                                showBottom = false
                            )
                            .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ScreenTitleWithCloseButton(
                            navController = navController,
                            title = "Filter Options",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 21.sp,
                            onClose = { navController.popBackStack() }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Date range field
                        FilterTextInputField(
                            label = "Date range :",
                            value = receivalForTimePeriodText,
                            onValueChange = { },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Calendar",
                                    tint = primary500,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            fromDateCustomFilter = null
                                            dateFilterSelected = true
                                        }
                                )
                            }
                        )

                        // Amount field
                        FilterTextInputField(
                            label = "Amount :",
                            state = amountState,
                            onValueChange = { },
                            placeholder = "Enter amount"
                        )

                        // Tranx ID field
                        FilterTextInputField(
                            label = "Tranx ID :",
                            state = tranxIdState,
                            onValueChange = { },
                            placeholder = "Enter transaction ID"
                        )

                        // Payment Type dropdown
                        FilterDropdownField(
                            navController = navController,
                            label = "Payment Type :",
                            value = selectedPaymentType,
                            options = listOf(ALL, "Tap", "QR", PAY_BY_LINK),
                            onValueChange = { selectedPaymentType = it }
                        )

                        // Tranx Type dropdown
                        FilterDropdownField(
                            navController = navController,
                            label = "Tranx Type :",
                            value = selectedTranxType,
                            options = listOf(ALL, "SALE", "REFUND", "VOID"),
                            onValueChange = { selectedTranxType = it }
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Apply Filters button - fixed at bottom
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP, vertical = 40.dp)
                ) {
                    FilledButton(
                        text = "APPLY FILTERS",
                        onClick = {
                            showFilterQuery = false
                            dateFilterSelected = false
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val amount = amountState.text.toString()
                                    val tranxId = tranxIdState.text.toString()
                                    val transctionType = getTransactionType(selectedTranxType)
                                    val deviceNumber = AppStorage.deviceNumber ?: getDeviceIdentifier()
                                    val uniqueCode = AppStorage.tokenCode ?: ""
                                    val insightsResponse = insights(
                                        deviceNumber = deviceNumber,
                                        uniqueCode = uniqueCode,
                                        startDate = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                                .replace('-', '/') + " 00:00:00"
                                        ,
                                        endDate = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                                .replace('-', '/') + " 23:59:59",
                                        transactionType = transctionType,
                                        productType = getProductType(selectedPaymentType),
                                        //dasmid = if( productType == ALL) null else productType,
                                        id = tranxId.ifEmpty { null },
                                        amount = amount.ifEmpty { null }
                                    )
                                    withContext(Dispatchers.Main) {
                                        if (insightsResponse != null) transactions =
                                            insightsResponse.data.records
                                        AppLogger.debug("insights Response -->: $insightsResponse")
                                    }
                                } catch (e: Exception) {
                                    AppLogger.debug("insights Error -->: $e")
                                } finally {
                                    withContext(Dispatchers.Main) {
                                        apiResponseAvailable = true
                                    }
                                }
                            }

                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )
                }
            }
        }
    } else {
        // Show search results with insights
        SimpleLayoutWithNavigation(navController, blurTopSection = true) {
            if (!apiResponseAvailable) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                       // .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                        .padding(top = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                            .background(Color.White)
                            .innerShadow(
                                color = innerShadow,
                                blur = 20.dp,
                                spread = 10.dp,
                                cornersRadius = 0.dp,
                                offsetX = 0.dp,
                                offsetY = 0.dp,
                                showBottom = false
                            )
                            .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                    ) {
                        DashboardStatsShimmer()
                        Spacer(modifier = Modifier.height(20.dp))
                        TransactionListShimmer(itemCount = 5)
                    }
                }
            } else {
                SearchResultsContent(
                    navController = navController,
                    transactions = transactions,
                    currency = currency,
                    receivalAmount = receivalAmount,
                    receivalForText = receivalForText,
                    receivalForTimePeriodText = receivalForTimePeriodText,
                    startDate = startDate,
                    endDate = endDate,
                    updateReceivalAmount = { updateReceivalAmount(it) },
                    onClickFilter = {
                        resetFilter()
                    }
                )
            }
        }
    }
}

@Composable
fun SearchResultsContent(
    navController: NavController,
    transactions: List<InsightsResponseDataRecord>,
    currency: String,
    receivalAmount: Float,
    receivalForText: String,
    receivalForTimePeriodText: String,
    startDate: OffsetDateTime,
    endDate: OffsetDateTime,
    updateReceivalAmount: (Float) -> Unit,
    onClickFilter: () -> Unit
) {
    var showBarChart by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
                    .innerShadow(
                        color = innerShadow,
                        blur = 20.dp,
                        spread = 10.dp,
                        cornersRadius = 0.dp,
                        offsetX = 0.dp,
                        offsetY = 0.dp,
                        showBottom = false
                    ),
                    //.padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Header with title and filter button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                    ,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Search Results",
                        color = primary500,
                        style = AppTheme.typography.titleNormal.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // Filter button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(borderThin, shape = RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .clickable { onClickFilter() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Filter",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = primary500
                        )
                    }
                }

                //Spacer(modifier = Modifier.height(10.dp))

                // Date filter and chart toggle
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(46.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {

//                    Row(
//                        Modifier
//                            .fillMaxHeight()
//                            .clip(RoundedCornerShape(8.dp))
//                            .background(iconBackgroundColor)
//                            .innerShadow(
//                                color = innerShadow,
//                                blur = 8.dp,
//                                spread = 5.dp,
//                                cornersRadius = 8.dp,
//                                offsetX = 0.dp,
//                                offsetY = 0.dp
//                            )
//                            .clickable(onClick = { showBarChart = !showBarChart })
//                            .zIndex(1f),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.SpaceEvenly
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Menu,
//                            contentDescription = "Show list",
//                            modifier = Modifier
//                                .padding(6.dp)
//                                .clip(RoundedCornerShape(4.dp))
//                                .background(if (!showBarChart) Color.White else Color.Transparent)
//                                .padding(4.dp)
//                        )
//                        Icon(
//                            imageVector = Icons.Default.BarChart,
//                            contentDescription = "Show bar graph",
//                            modifier = Modifier
//                                .padding(6.dp)
//                                .clip(RoundedCornerShape(4.dp))
//                                .background(if (showBarChart) Color.White else Color.Transparent)
//                                .padding(4.dp)
//                        )
//                    }
//                }

                //Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                       // .padding(horizontal = if (showBarChart) DEFAULT_BOTTOM_SECTION_PADDING_IN_DP else 0.dp)
                        .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
                ) {
//                    Text(
//                        text = receivalForText,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        color = primary900,
//                    )
//
//                    Spacer(modifier = Modifier.height(4.dp))
//
//                    Text(
//                        text = receivalForTimePeriodText,
//                        style = AppTheme.typography.footnote,
//                    )
//
//                    Spacer(modifier = Modifier.height(4.dp))
//
//                    CurrencyText(
//                        currency = currency,
//                        amount = receivalAmount.formatToPrecisionString(),
//                        fontWeight = FontWeight(980)
//                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .conditional(true) { verticalScroll(scrollState) }) {

                        if (showBarChart) TransactionsGroupedBarChart(
                            navController = navController,
                            transactions = transactions,
                            startDateMillis = startDate.toInstant().toEpochMilli(),
                            endDateMillis = endDate.toInstant().toEpochMilli(),
                            currency = currency,
                            updateReceivalAmount = {
                                updateReceivalAmount(it)
                            }
                        ) else Transactions(
                            navController, transactions = transactions,
                            updateReceivalAmount = {
                                updateReceivalAmount(it)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterTextInputField(
    label: String,
    value: String = "",
    state: TextFieldState? = null,
    onValueChange: (String) -> Unit?,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    textFieldHeight: Dp = 46.dp,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = primary500,
            modifier = Modifier.weight(0.35f)
        )
        if (trailingIcon != null) {
            FilterTextField(value, placeholder, trailingIcon)
        } else {
            Box(
                Modifier
                    .weight(0.65f)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .border(1.dp, Color.Blue.copy(alpha = 0.5f),shape = RoundedCornerShape(8.dp))
            ) {
                TextField(
                    state = state!!,
                    keyboardOptions = KeyboardOptions.Default,
                    placeholder = {
                        Text(
                            placeholder,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = primary500.copy(alpha = 0.2f)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        focusedTextColor = primary500,
                        focusedIndicatorColor = Color.Transparent,

                        unfocusedContainerColor = Color.White,
                        unfocusedTextColor = purple50,
                        unfocusedIndicatorColor = Color.Transparent,

                        errorContainerColor = red300.copy(alpha = 0.1f),
                        errorTextColor = red300,
                        errorIndicatorColor = Color.Transparent,
                        errorCursorColor = red300.copy(alpha = 0.1f),
                    ),
                    modifier = Modifier
                        .height(textFieldHeight)
                        .onFocusChanged { },
//                        .innerShadow(
//                            blur = 16.dp,
//                            color = innerShadow,
//                            cornersRadius = 6.dp,
//                            offsetX = 0.5.dp,
//                            offsetY = 0.5.dp
//                        ),
                    lineLimits = TextFieldLineLimits.SingleLine,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp))

            }
        }
    }

}

@Composable
private fun RowScope.FilterTextField(
    value: String,
    placeholder: String,
    trailingIcon: @Composable (() -> Unit)
) {
    Box(
        modifier = Modifier
            .weight(0.65f)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color.Blue.copy(alpha = 0.5f),shape = RoundedCornerShape(8.dp))
//            .innerShadow(
//                color = innerShadow,
//                blur = 8.dp,
//                spread = 5.dp,
//                cornersRadius = 8.dp,
//                offsetX = 0.dp,
//                offsetY = 0.dp
//            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(.9f)) {
                Text(
                    text = if (value.isEmpty() && placeholder.isNotEmpty()) placeholder else value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (value.isEmpty() && placeholder.isNotEmpty())
                        primary500.copy(alpha = 0.3f)
                    else
                        primary500
                )
            }

            Box(Modifier.weight(.1f)) {
                trailingIcon()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdownField(
    navController: NavController,
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = primary900,
            modifier = Modifier.weight(0.35f)
        )

        Box(
            modifier = Modifier
                .weight(0.65f)
                //.shape(RoundedCornerShape(8.dp))
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .border(1.dp, Color.Blue.copy(alpha = 0.5f),shape = RoundedCornerShape(8.dp))
//                .background(iconBackgroundColor)
//                .innerShadow(
//                    color = innerShadow,
//                    blur = 8.dp,
//                    spread = 5.dp,
//                    cornersRadius = 8.dp,
//                    offsetX = 0.dp,
//                    offsetY = 0.dp
//                )
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = primary500
                )

                Icon(
                    painter = painterResource(R.drawable.down_arrow),
                    contentDescription = "Dropdown",
                    tint = primary500,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (expanded) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = { expanded = false },
            sheetState = sheetState,
            containerColor = Color.White,
            contentColor = primary500,
            dragHandle = {}
        ) {
            Box(
                modifier = Modifier
                    .innerShadow(
                        color = innerShadow,
                        blur = 20.dp,
                        spread = 10.dp,
                        cornersRadius = 0.dp,
                        offsetX = 0.dp,
                        offsetY = 0.dp
                    )
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                    verticalArrangement = Arrangement.Top
                ) {
                    ScreenTitleWithCloseButton(
                        navController = navController,
                        title = label.removeSuffix(" :"),
                        onClose = { expanded = false },
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP,
                                vertical = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
                            )
                            .background(Color.White)
                    )

                    options.forEachIndexed { index, option ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP,
                                        end = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP.plus(10.dp)
                                    )
                                    .clickable {
                                        onValueChange(option)
                                        expanded = false
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = primary500
                                )

                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color.White, shape = RoundedCornerShape(50))
                                        .border(borderThin, shape = RoundedCornerShape(50)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (value == option) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .background(
                                                    primary500,
                                                    shape = RoundedCornerShape(50)
                                                )
                                        )
                                    }
                                }
                            }

                            if (index != options.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.fillMaxWidth(),
                                    thickness = 2.dp,
                                    color = Color.LightGray.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
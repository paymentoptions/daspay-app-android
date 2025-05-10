import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun RefundScreen(navController: NavController) {
//    var showMerchantIdDialog by remember { mutableStateOf(true) }
//    var showTransactionIdDialog by remember { mutableStateOf(false) }
//    var rawInput by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//    var merchantId by remember { mutableStateOf("") }
//    var transactionId by remember { mutableStateOf("") }
//
//    fun formatAmount(input: String): String {
//        if (input.isEmpty()) return "0.00"
//        val cents = input.toLong()
//        val dollars = cents / 100
//        val centPortion = (cents % 100).toString().padStart(2, '0')
//        return "$dollars.$centPortion"
//    }
//
//    val formattedAmount = formatAmount(rawInput)
//
//    val onKeyPress: (String) -> Unit = { key ->
//        when (key) {
//            "←" -> {
//                if (rawInput.isNotEmpty()) {
//                    rawInput = rawInput.dropLast(1)
//                }
//            }
//
//            else -> {
//                // Avoid too many digits
//                if (rawInput.length < 9) {
//                    rawInput += key
//                }
//            }
//        }
//    }
//    val background = Color(0xFF121017)
//    val buttonColor = Color(0xFF1E1E1E)
//    val buttonBorder = Color(0xFF3A3A3A)
//    val textColor = Color(0xFFEFEFEF)
//    val buttons = listOf(
//        listOf("1", "2", "3"),
//        listOf("4", "5", "6"),
//        listOf("7", "8", "9"),
//        listOf("00", "0", "←")
//    )
//    val context = LocalContext.current
//    val activity = context as? Activity
//
//    val launcher = rememberLauncherForActivityResult(
//        HeadlessActivity.contract(ClientHeadlessImpl::class.java)
//    )
//    {
//
//        var completedSaleTranId: String? = ""
//        var completedSalePosReference: String? = ""
//        var completedSaleRequestId: String? = ""
//
//        when (it) {
//            is WrappedResult.Success -> {
//                if (it.value.tranType == TranType.SALE) {
//
//                    completedSaleTranId = it.value.tranId
//                    completedSalePosReference = it.value.posReference
//                    completedSaleRequestId = it.value.actions.firstOrNull()?.requestId
//                }
//
//                Log.d(
//                    "Success ->",
//                    "completedSaleTranId: $completedSaleTranId | completedSalePosReference: $completedSalePosReference | completedSaleRequestId: $completedSaleRequestId | it: ${it.value}"
//                )
//
//                Toast.makeText(
//                    context,
//                    "Transaction of $$formattedAmount was successful",
//                    Toast.LENGTH_LONG
//                ).show()
//
//                rawInput = ""
//
////                    if (it.value.tranType == TranType.REFUND) {
////                        completedRefundTranId = it.value.tranId
////                    }
//            }
//
//            is WrappedResult.Failure -> {
////                    viewModel.writeMessage("Failed")
//                Log.d("Failed ->", "Payment Failed")
//                Toast.makeText(
//                    context,
//                    "Transaction of $$formattedAmount was failed",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(background)
//            .padding(20.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.SpaceBetween,
//    ) {
//
//
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//
//            Text("Refund", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = "$$formattedAmount",
//                fontSize = 48.sp,
//                fontWeight = FontWeight.ExtraBold,
//                color = Color(0xFFAAB2C8)
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//
//            OutlinedTextField(
//                value = description,
//                onValueChange = { description = it },
//                label = { Text("Add description", color = Color.Gray) },
//                modifier = Modifier
//                    .width(160.dp)
//                    .height(60.dp),
//                shape = RoundedCornerShape(12.dp),
//                singleLine = true,
//                textStyle = TextStyle(
//                    color = textColor,
//                    fontSize = 12.sp,
//                    fontStyle = FontStyle.Normal,
//                    textDecoration = TextDecoration.None
//                ),
//                colors = TextFieldDefaults.colors(
//                    unfocusedContainerColor = background,
//                    focusedContainerColor = background,
//                    focusedIndicatorColor = Orange10,
//                    focusedLabelColor = Color(0xFFFF9800),
//                    unfocusedLabelColor = Color.DarkGray,
//                    focusedTextColor = Color.Black,
//                    unfocusedTextColor = Color.Black,
//                    errorContainerColor = Color.White,
//                )
//            )
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//
//            buttons.forEach { row ->
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    row.forEach { key ->
//                        Box(
//                            modifier = Modifier
//                                .weight(1f)
//                                .aspectRatio(1.4f)
//                                .padding(horizontal = 5.dp)
//                                .border(1.dp, buttonBorder, RoundedCornerShape(8.dp))
//                                .background(buttonColor, RoundedCornerShape(8.dp))
//                                .clickable { onKeyPress(key) },
//                            contentAlignment = Alignment.Center
//                        ) {
//                            if (key == "←") {
//                                Icon(
//                                    Icons.Default.ArrowBack,
//                                    contentDescription = "Back",
//                                    tint = Color.White
//                                )
//                            } else {
//                                Text(
//                                    key,
//                                    color = Color.White,
//                                    fontSize = 22.sp,
//                                    fontWeight = FontWeight.Bold
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            TextInputDialog(
//                showDialog = showMerchantIdDialog,
//                title = "Merchant ID",
//                label = "Merchant Id",
//                value = merchantId,
//                onValueChange = {
//                    merchantId = it
//                },
//                acceptButtonText = "OK",
//                cancelButtonText = "Cancel",
//                onAccept = {
//                    showMerchantIdDialog = false
//                },
//                onDismiss = {
//                    merchantId = ""
//                    showMerchantIdDialog = false
//                    navController.popBackStack()
//                },
//            )
//
//            TextInputDialog(
//                showDialog = showTransactionIdDialog,
//                title = "Transaction Id",
//                label = "Transaction Id",
//                value = transactionId,
//                onValueChange = {
//                    transactionId = it
//                },
//                acceptButtonText = "OK",
//                cancelButtonText = "Cancel",
//                onAccept = {
//                    try {
//                        val refundResponse = refund(
//                            context,
//                            transactionId,
//                            merchantId,
//                            formattedAmount.toFloat(),
//                            description
//                        )
//
//                        if (refundResponse.success) {
//                            Toast.makeText(
//                                context,
//                                "Refund of $$formattedAmount processed successfully",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        } else {
//                            Toast.makeText(
//                                context,
//                                refundResponse.gateway_response.message,
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//                    } catch (e: Exception) {
//                        println("refundScreen: ${e.message}")
//                    } finally {
//                        showTransactionIdDialog = false
//                    }
//                },
//                onDismiss = {
//                    transactionId = ""
//                    showTransactionIdDialog = false
//                },
//            )
//
//            // Charge Button
//            val chargeEnabled = rawInput.isNotEmpty() && rawInput.toLong() > 0 && merchantId !== ""
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(48.dp)
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(if (chargeEnabled) Color(0xFFFF9800) else Color(0xFF2B2B2B))
//                    .clickable(enabled = chargeEnabled) {
//                        showTransactionIdDialog = true
//                    },
//                contentAlignment = Alignment.Center,
//
//                ) {
//                Text(
//                    "Refund",
//                    color = if (chargeEnabled) Color.White else Color.Gray,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        }
//
//
//        Text(
//            "©2025, Payment Options Limited", color = Color.Gray, fontSize = 12.sp
//        )
//    }
}
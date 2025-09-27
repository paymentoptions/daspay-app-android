package com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow

import MyDialog
import android.content.Intent
import android.os.Handler
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import co.yml.charts.common.extensions.isNotNull
import com.paymentoptions.pos.R
import com.paymentoptions.pos.device.DeveloperOptions
import com.paymentoptions.pos.device.Nfc
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.device.getApms
import com.paymentoptions.pos.device.getTransactionCurrency
import com.paymentoptions.pos.device.screenRatioToDp
import com.paymentoptions.pos.services.apiService.CategoryListDataRecord
import com.paymentoptions.pos.services.apiService.PayByLinkRequest
import com.paymentoptions.pos.services.apiService.PayByLinkRequestProduct
import com.paymentoptions.pos.services.apiService.PayByLinkResponse
import com.paymentoptions.pos.services.apiService.endpoints.categoryList
import com.paymentoptions.pos.services.apiService.endpoints.payByLink
import com.paymentoptions.pos.services.apiService.endpoints.productList
import com.paymentoptions.pos.ui.composables._components.MyCircularProgressIndicator
import com.paymentoptions.pos.ui.composables._components.NoteChip
import com.paymentoptions.pos.ui.composables._components.buttons.Email
import com.paymentoptions.pos.ui.composables._components.buttons.EmailButton
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.buttons.ScanButton
import com.paymentoptions.pos.ui.composables._components.buttons.ShareButton
import com.paymentoptions.pos.ui.composables._components.images.PayByLinkImage
import com.paymentoptions.pos.ui.composables._components.images.PaymentQrCodeImage
import com.paymentoptions.pos.ui.composables._components.images.PaymentTapToPayImage
import com.paymentoptions.pos.ui.composables._components.paymentimagerow.PaymentApmsRow
import com.paymentoptions.pos.ui.composables._components.paymentimagerow.PaymentSchemesRow
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_HEIGHT_IN_DP
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.additionalcharge.AdditionalChargeBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.foodmenu.FoodMenuBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.foodmenu.ToastData
import com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.foodmenu.ToastType
import com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.reviewcart.ReviewCartBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow.chargemoney.ChargeMoneyBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens.status.MessageForStatusScreen
import com.paymentoptions.pos.ui.composables.screens.status.StatusScreen
import com.paymentoptions.pos.ui.composables.screens.status.StatusScreenType
import com.paymentoptions.pos.ui.theme.green100
import com.paymentoptions.pos.ui.theme.green500
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.ui.theme.red300
import com.paymentoptions.pos.ui.theme.red500
import com.paymentoptions.pos.utils.PaymentMethod
import com.paymentoptions.pos.utils.cashPaymentMethod
import com.paymentoptions.pos.utils.formatToPrecisionString
import com.paymentoptions.pos.utils.paymentMethods
import com.paymentoptions.pos.utils.qrCodePaymentMethod
import com.paymentoptions.pos.utils.tapPaymentMethod
import com.paymentoptions.pos.utils.viaLinkPaymentMethod
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt

const val MAX_QUANTITY_PER_FOOD_ITEM = 20

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodOrderFlow(
    navController: NavController,
    initialFoodOrderFlowStage: FoodOrderFlowStage = FoodOrderFlowStage.MENU,
) {
    val context = LocalContext.current
    val currency = getTransactionCurrency(context)
    val enableScrollingInsideBottomSectionContent = true

    var foodOrderFlowStage by remember {
        mutableStateOf<FoodOrderFlowStage>(
            initialFoodOrderFlowStage
        )
    }

    var foodCategoryList by remember { mutableStateOf<List<CategoryListDataRecord>>(listOf()) }
    var selectedFoodCategory by remember { mutableStateOf<CategoryListDataRecord?>(null) }
    var foodCategoryListAvailable by remember { mutableStateOf(false) }
    var foodItemListAvailable by remember { mutableStateOf(false) }
    var startTapAndPay by remember { mutableStateOf(false) }
    var apms by remember { mutableStateOf(getApms(context)) }
    var paymentUrl by remember { mutableStateOf("") }

    var cartState by remember {
        mutableStateOf<Cart>(
            Cart(
                serviceChargePercentage = 10f,
                gstPercentage = 9f,
                additionalCharge = 0f,
                additionalAmountNote = ""
            )
        )
    }

    LaunchedEffect(Unit) {
        val savedCart = SharedPreferences.getCart(context)
        if (savedCart.isNotNull()) cartState = savedCart!!
    }

    val scrollState = rememberScrollState()
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod>(paymentMethods.first()) }
    var nfcStatusPair by remember { mutableStateOf(Nfc.getStatus(context)) }
    var showDeveloperOptionsEnabled by remember { mutableStateOf(false) }
    var showNFCNotEnabled by remember { mutableStateOf(false) }

    var toastData by remember { mutableStateOf(ToastData()) }
    var showToast by remember { mutableStateOf(false) }

    val pxToMove = with(LocalDensity.current) {
        20.times(-1).dp.toPx().roundToInt()
    }

    val offset by animateIntOffsetAsState(
        targetValue = if (showToast) {
            IntOffset(0, pxToMove)
        } else {
            IntOffset.Zero
        }, animationSpec = keyframes { durationMillis = 1000 }, label = "offset"
    )

    val alpha by animateFloatAsState(
        targetValue = if (showToast) 1f else 0f, animationSpec = keyframes {
            durationMillis = 1000
            1.0f at 0 using LinearOutSlowInEasing
            0f at 1000 using LinearOutSlowInEasing
        }, label = "keyframe"
    )

    fun setShowToast(show: Boolean) {
        showToast = show
    }

    fun updateFlowStage(newFoodOrderFlowStage: FoodOrderFlowStage) {
        foodOrderFlowStage = newFoodOrderFlowStage
    }

    if (!nfcStatusPair.first) {
        tapPaymentMethod.setIsEnabled(false)
        Toast.makeText(context, "Your device does not support NFC", Toast.LENGTH_SHORT).show()
    }

    if (!apms.hasPayEasy && !apms.hasGooglePay && !apms.hasPayPay && !apms.hasWechatpay && !apms.hasKonbini && !apms.hasAlipay && !apms.hasGCash && !apms.hasDinersClub) {
        qrCodePaymentMethod.setIsEnabled(false)
        Toast.makeText(context, "Payment via QR code not supported", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) {
        foodCategoryListAvailable = false
        try {
            val foodCategoryListFromAPI = categoryList(context)

            if (foodCategoryListFromAPI != null) foodCategoryList =
                foodCategoryListFromAPI.data.records

            selectedFoodCategory = foodCategoryList.firstOrNull()

        } catch (e: Exception) {
            Toast.makeText(
                context, "Error fetching food categories from API", Toast.LENGTH_SHORT
            ).show()

            if (e.toString().contains("HTTP 401")) navController.navigate(Screens.SignIn.route) {
                popUpTo(0) { inclusive = true }
            }
        } finally {
            foodCategoryListAvailable = true
        }
    }

    if (foodCategoryListAvailable) LaunchedEffect(selectedFoodCategory) {

        foodItemListAvailable = false

        if (selectedFoodCategory.isNotNull()) {
            try {
                val foodItemListFromAPI = productList(context, selectedFoodCategory!!.CategoryID)

                if (foodItemListFromAPI != null) {
                    val newFoodItems = foodItemListFromAPI.data.records.map { record ->
                        FoodItem(item = record)
                    }

                    cartState.replaceFoodCategory(
                        categoryId = selectedFoodCategory!!.CategoryID, newFoodItems, context
                    )

                } else cartState.replaceFoodCategory(
                    selectedFoodCategory!!.CategoryID, listOf<FoodItem>(), context
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Error fetching products from API", Toast.LENGTH_SHORT)
                    .show()

                if (e.toString()
                        .contains("HTTP 401")
                ) navController.navigate(Screens.SignIn.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
        foodItemListAvailable = true
    }

    if (showToast) Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 220.dp)
            .offset { offset }
            .padding(horizontal = 50.dp)
            .background(Color.Transparent)
            .zIndex(10f),
        contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (toastData.type == ToastType.SUCCESS) green100.copy(alpha = alpha) else red300.copy(
                        alpha = alpha
                    ), shape = RoundedCornerShape(10.dp)
                )
                .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .align(alignment = Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.background(
                    if (toastData.type == ToastType.SUCCESS) green500.copy(alpha = alpha) else red500.copy(
                        alpha = alpha
                    ), shape = RoundedCornerShape(2.dp)
                )

            ) {
                Text(
                    toastData.cartCount.toString(),
                    color = Color.White.copy(alpha = alpha),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Text(
                toastData.text,
                color = if (toastData.type == ToastType.SUCCESS) green500.copy(alpha = alpha) else red500.copy(
                    alpha = alpha
                ),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }

    when (foodOrderFlowStage) {
        FoodOrderFlowStage.MENU -> SectionedLayout(
            navController = navController,
            bottomBarContent = BottomBarContent.TOGGLE_BUTTON,
            bottomSectionPaddingInDp = 0.dp,
            bottomSectionMinHeightRatio = 0.9f,
            enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent
        ) {
            FoodMenuBottomSectionContent(
                navController,
                enableScrolling = enableScrollingInsideBottomSectionContent,
                foodCategoriesAvailable = foodCategoryListAvailable,
                foodCategories = foodCategoryList,
                selectedFoodCategory = selectedFoodCategory,
                updateSelectedFoodCategory = { selectedFoodCategory = it },
                foodItemsAvailable = foodItemListAvailable,
                cartState = cartState,
                updateCartSate = { cartState = it.copy() },
                updateFlowStage = { updateFlowStage(it) },
                createToast = { toastData.setToast(it) },
                setShowToast = { setShowToast(it) })
        }

        FoodOrderFlowStage.REVIEW_CART -> {
            SectionedLayout(
                navController = navController,
                bottomBarContent = BottomBarContent.TOGGLE_BUTTON,
                bottomSectionPaddingInDp = 0.dp,
                bottomSectionMinHeightRatio = 0.9f,
                enableScrollingOfBottomSectionContent = enableScrollingInsideBottomSectionContent
            ) {
                ReviewCartBottomSectionContent(
                    navController,
                    enableScrolling = !enableScrollingInsideBottomSectionContent,
                    cartState = cartState,
                    updateCartSate = { cartState = it.copy() },
                    updateFlowStage = { updateFlowStage(it) },
                    createToast = { toastData.setToast(it) },
                    setShowToast = { setShowToast(it) })
            }
        }

        FoodOrderFlowStage.ADDITIONAL_CHARGE -> {
            SectionedLayout(
                navController = navController,
                bottomSectionMaxHeightRatio = 0.95f,
                bottomBarContent = BottomBarContent.TOGGLE_BUTTON,
                bottomSectionPaddingInDp = 0.dp,
                enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent
            ) {
                AdditionalChargeBottomSectionContent(
                    navController,
                    enableScrolling = enableScrollingInsideBottomSectionContent,
                    cartState = cartState,
                    updateCartSate = { cartState = it.copy() },
                    updateFlowStage = { updateFlowStage(it) })
            }
        }

        FoodOrderFlowStage.CHARGE_MONEY -> {

            SectionedLayout(
                navController = navController,
                bottomSectionMinHeightRatio = 0.25f,
                bottomBarContent = BottomBarContent.TOGGLE_BUTTON,
                bottomSectionPaddingInDp = 0.dp,
                enableScrollingOfBottomSectionContent = false,
                imageBelowLogo = {
                    Column(
                        modifier = Modifier
                            .height(screenRatioToDp(0.5f))
                            .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (selectedPaymentMethod) {
                            tapPaymentMethod -> {
                                val currentNfcStatusPair = Nfc.getStatus(context)

                                if (DeveloperOptions.isEnabled(context)) showDeveloperOptionsEnabled =
                                    true
                                else if (!nfcStatusPair.second) showNFCNotEnabled = true
                                else if (!currentNfcStatusPair.second) showNFCNotEnabled = true

                                MyDialog(
                                    showDialog = showDeveloperOptionsEnabled,
                                    title = "Caution",
                                    text = "You need to disable developer options to proceed further.",
                                    acceptButtonText = "Developer Options",
                                    cancelButtonText = "Cancel",
                                    onAcceptFn = {
                                        showDeveloperOptionsEnabled = false
                                        val intent =
                                            Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                                        context.startActivity(intent)
                                    },
                                    onDismissFn = {
                                        showDeveloperOptionsEnabled = false
                                        selectedPaymentMethod = qrCodePaymentMethod
                                    },
                                )

                                MyDialog(
                                    showDialog = showNFCNotEnabled,
                                    title = "NFC Required",
                                    text = "This feature needs NFC. Please enable it in your device settings.",
                                    acceptButtonText = "Go to Settings",
                                    cancelButtonText = "Cancel",
                                    onAcceptFn = {
                                        showNFCNotEnabled = false
                                        val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                                        context.startActivity(intent)
                                    },
                                    onDismissFn = {
                                        showNFCNotEnabled = false
                                        selectedPaymentMethod = qrCodePaymentMethod
                                    },
                                )

                                PaymentTapToPayImage(
                                    modifier = Modifier
                                        .padding(horizontal = 20.dp)
                                        .fillMaxWidth()
                                        .height(230.dp)
                                        .clip(shape = RoundedCornerShape(16.dp))
                                        .clickable { startTapAndPay = true })

                                FilledButton(
                                    text = "Tap here to start Tap To Pay",
                                    onClick = { startTapAndPay = true },
                                    modifier = Modifier
                                        .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                                        .height(59.dp)
                                        .scale(0.8f)
                                )

                                PaymentSchemesRow(modifier = Modifier.height(50.dp))
                            }

                            qrCodePaymentMethod -> {

                                Text(
                                    text = "Scan QR Code",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                )

                                PaymentQrCodeImage(
                                    modifier = Modifier
                                        .padding(horizontal = 20.dp)
                                        .fillMaxWidth()
                                        .height(240.dp)
                                        .clip(
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                )

                                PaymentApmsRow(modifier = Modifier.height(50.dp))

                                NoteChip(
                                    text = "Ask customer to scan with GrabPay",
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                                )
                            }

                            cashPaymentMethod -> {
                                Text(
                                    text = "Please pay cash",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            viaLinkPaymentMethod -> {

                                var payByLinkRequest = PayByLinkRequest(
                                    PBLLinkName = "PayByLink Test",
                                    ExpiryDate = SimpleDateFormat("YYYY-dd MMMM, YYYY HH:mm:ss").format(
                                        Date()
                                    ),
                                    Product = listOf<PayByLinkRequestProduct>(
                                        PayByLinkRequestProduct(
                                            Currency = currency,
                                            Name = "No Name",
                                            Quantity = 1,
                                            Price = 100f,
                                            TotalPrice = "100"
                                        )
                                    )
                                )
                                var payByLinkResponse by remember {
                                    mutableStateOf<PayByLinkResponse?>(
                                        null
                                    )
                                }
                                var payByLinkApiResponseLoading by remember { mutableStateOf(false) }
                                var payByLinkScanCodeBottomSheetExpanded by remember {
                                    mutableStateOf(
                                        true
                                    )
                                }
                                val sheetState = rememberModalBottomSheetState()

                                LaunchedEffect(Unit) {
                                    try {
                                        payByLinkApiResponseLoading = true
                                        val dasmid = com.paymentoptions.pos.device.getPayByLinkDasmid(context)
//                                      payByLinkResponse = payByLink(context, payByLinkRequest)
                                        payByLinkResponse = payByLink(context, payByLinkRequest, dasmid)

                                        println("payByLinkResponse: $payByLinkResponse")

                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Error generating payment link...",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } finally {
                                        payByLinkApiResponseLoading = false
                                    }
                                }

                                if (payByLinkApiResponseLoading) MyCircularProgressIndicator()
                                else if (payByLinkResponse.isNotNull()) {

                                    if (payByLinkScanCodeBottomSheetExpanded) ModalBottomSheet(
                                        modifier = Modifier.fillMaxWidth(),
                                        onDismissRequest = {
                                            payByLinkScanCodeBottomSheetExpanded = false
                                        },
                                        sheetState = sheetState,
                                        containerColor = Color.White,
                                        contentColor = primary500,
                                        dragHandle = {}) {

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 20.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.logo),
                                                contentDescription = "DASPay Logo",
                                                tint = primary500,
                                                modifier = Modifier
                                                    .height(
                                                        LOGO_HEIGHT_IN_DP.div(1.5f)
                                                    )
                                                    .align(Alignment.Center)
                                            )

                                            IconButton(
                                                modifier = Modifier.align(alignment = Alignment.CenterEnd),
                                                onClick = {
                                                    payByLinkScanCodeBottomSheetExpanded = false
                                                }) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Close",
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }

                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {

                                            PaymentQrCodeImage(
                                                modifier = Modifier
                                                    .padding(horizontal = 20.dp)
                                                    .fillMaxWidth()
                                                    .height(220.dp)
                                                    .clip(shape = RoundedCornerShape(16.dp))
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            NoteChip(
                                                text = "Scan with your device",
                                                modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Pay Via Link",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 18.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        PayByLinkImage(
                                            modifier = Modifier
                                                .padding(horizontal = 20.dp)
                                                .fillMaxWidth()
                                                .height(110.dp)
                                                .clip(
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    color = Color(0xFFDCEAFE),
                                                    shape = RoundedCornerShape(11.dp)
                                                )
                                                .padding(vertical = 8.dp, horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {

                                            SelectionContainer {
//                                            LinkWithIcon(
//                                                text = "https://daspay/" + payByLinkResponse!!.data.ID,
//                                                url = "https://daspay/" + payByLinkResponse!!.data.ID,
//                                            )

                                                Text(
//                                                    text = "https://daspay/" + payByLinkResponse!!.data.ID,
                                                    text = "https://api-dev.paymentoptions.com/paybylink/" + payByLinkResponse!!.data.ProductID,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 18.sp,
                                                    color = primary900,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }

                                        NoteChip(
                                            text = "Share this link with the customer",
                                            color = Color.White
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            EmailButton(
                                                text = "Email",
                                                email = Email(
                                                    subject = "DASPay payment Link",
                                                    text = paymentUrl
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .border(
                                                        2.dp,
                                                        color = primary100.copy(alpha = 0.2f),
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .background(
                                                        Color.White,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .padding(horizontal = 10.dp, vertical = 20.dp)
                                            )

                                            ShareButton(
                                                text = "Share",
                                                shareContent = paymentUrl,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .border(
                                                        2.dp,
                                                        color = primary100.copy(alpha = 0.2f),
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .background(
                                                        Color.White,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .padding(horizontal = 10.dp, vertical = 20.dp)
                                            )

                                            ScanButton(
                                                text = "Scan",
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .border(
                                                        2.dp,
                                                        color = primary100.copy(alpha = 0.2f),
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .background(
                                                        Color.White,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .padding(horizontal = 10.dp, vertical = 20.dp)
                                                    .clickable {
                                                        payByLinkScanCodeBottomSheetExpanded = true
                                                    })
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Error generating payment link. Try again after some time...",
                                        color = red300,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                }
                            }
                        }
                    }
                }) {
                ChargeMoneyBottomSectionContent(
                    navController,
                    enableScrolling = false,
                    amountToCharge = cartState.calculateGrandTotal().formatToPrecisionString(),
                    selectedPaymentMethod = selectedPaymentMethod,
                    updateSelectedPaymentMethod = { selectedPaymentMethod = it },
                    updateFlowStage = { updateFlowStage(it as FoodOrderFlowStage) },
                    onChangeAmount = { updateFlowStage(FoodOrderFlowStage.REVIEW_CART) },
                    startTapAndPay = startTapAndPay,
                    turnoffStartTapToPay = { startTapAndPay = false })
            }
        }

        FoodOrderFlowStage.RESULT_PROCESSING -> {

            val dataMessage = MessageForStatusScreen(
                text = "Processing...", statusScreenType = StatusScreenType.PROCESSING
            )
            StatusScreen(navController, dataMessage, strategyFn = {
//                Handler().postDelayed({
//                    updateRefundStatus(StatusScreenType.SUCCESS)
//                }, 2000)
            })

        }

        FoodOrderFlowStage.RESULT_ERROR -> {

            val dataMessage = MessageForStatusScreen(
                text = "Payment Failed", statusScreenType = StatusScreenType.ERROR
            )
            StatusScreen(navController, dataMessage, strategyFn = {
                Handler().postDelayed({
//                    updateRefundStatus(null)
//
//                    Toast.makeText(
//                        context,
//                        "Error processing refund. Try again..",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }, 2000)
            })
        }

        FoodOrderFlowStage.RESULT_SUCCESS -> {
            Cart.clearSavedCart(context)
            val dataMessage = MessageForStatusScreen(
                text = "Payment Successful", statusScreenType = StatusScreenType.SUCCESS
            )
            StatusScreen(navController, dataMessage, strategyFn = {
//                Handler().postDelayed({
////                    updateRefundStatus(StatusScreenType.ERROR)
//                    navController.navigate(Screens.RefundInitiated.route)
//                }, 2000)
            })
        }
    }
}
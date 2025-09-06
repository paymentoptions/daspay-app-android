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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import co.yml.charts.common.extensions.isNotNull
import com.paymentoptions.pos.device.DeveloperOptions
import com.paymentoptions.pos.device.Nfc
import com.paymentoptions.pos.device.SharedPreferences
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
import com.paymentoptions.pos.ui.composables._components.buttons.ScanButton
import com.paymentoptions.pos.ui.composables._components.buttons.ShareButton
import com.paymentoptions.pos.ui.composables._components.images.PayByLinkImage
import com.paymentoptions.pos.ui.composables._components.images.PaymentQrCodeImage
import com.paymentoptions.pos.ui.composables._components.images.PaymentTapToPayImage
import com.paymentoptions.pos.ui.composables._components.images.cardpayment.AmexImage
import com.paymentoptions.pos.ui.composables._components.images.cardpayment.JcbImage
import com.paymentoptions.pos.ui.composables._components.images.cardpayment.MastercardImage
import com.paymentoptions.pos.ui.composables._components.images.cardpayment.VisaImage
import com.paymentoptions.pos.ui.composables._components.images.qrpayment.AliPayImage
import com.paymentoptions.pos.ui.composables._components.images.qrpayment.ApplePayImage
import com.paymentoptions.pos.ui.composables._components.images.qrpayment.GrabPayImage
import com.paymentoptions.pos.ui.composables._components.images.qrpayment.QrPayment2
import com.paymentoptions.pos.ui.composables._components.images.qrpayment.QrPayment3
import com.paymentoptions.pos.ui.composables._components.images.qrpayment.WechatPayImage
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
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

                                if (DeveloperOptions.isEnabled(context)) showDeveloperOptionsEnabled =
                                    true
                                else if (!nfcStatusPair.second) showNFCNotEnabled = true

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
                                        .clip(
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                )

                                Text(
                                    text = "Tap To Pay",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.height(60.dp)
                                ) {
                                    VisaImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(8.dp))
                                            .weight(1f)
                                    )

                                    MastercardImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(8.dp))
                                            .weight(1f)
                                    )

                                    AmexImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(8.dp))
                                            .weight(1f)
                                    )

                                    JcbImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(8.dp))
                                            .weight(1f)
                                    )
                                }
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

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.height(50.dp)
                                ) {
                                    GrabPayImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )

                                    QrPayment2(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )

                                    QrPayment3(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )

                                    AliPayImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )

                                    ApplePayImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )

                                    WechatPayImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )
                                }

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

                                LaunchedEffect(Unit) {
                                    try {
                                        payByLinkApiResponseLoading = true
                                        payByLinkResponse = payByLink(context, payByLinkRequest)

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
                                                    text = "https://daspay/" + payByLinkResponse!!.data.ID,
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
                                                email = Email(),
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
                                            )
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
                    onChangeAmount = { updateFlowStage(FoodOrderFlowStage.REVIEW_CART) })
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
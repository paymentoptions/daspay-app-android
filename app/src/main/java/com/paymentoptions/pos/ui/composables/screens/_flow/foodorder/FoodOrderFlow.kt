package com.paymentoptions.pos.ui.composables.screens._flow.foodorder

import MyDialog
import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import co.yml.charts.common.extensions.isNotNull
import com.paymentoptions.pos.device.DeveloperOptions
import com.paymentoptions.pos.device.Nfc
import com.paymentoptions.pos.device.screenRatioToDp
import com.paymentoptions.pos.services.apiService.CategoryListDataRecord
import com.paymentoptions.pos.services.apiService.endpoints.categoryList
import com.paymentoptions.pos.services.apiService.endpoints.productList
import com.paymentoptions.pos.ui.composables._components.images.PaymentQrCodeImage
import com.paymentoptions.pos.ui.composables._components.images.PaymentTapToPayImage
import com.paymentoptions.pos.ui.composables._components.images.cardpayment.AmexImage
import com.paymentoptions.pos.ui.composables._components.images.cardpayment.JcbImage
import com.paymentoptions.pos.ui.composables._components.images.cardpayment.MastercardImage
import com.paymentoptions.pos.ui.composables._components.images.cardpayment.VisaImage
import com.paymentoptions.pos.ui.composables._components.images.qrpayment.ApplePayImage
import com.paymentoptions.pos.ui.composables._components.images.qrpayment.GrabPayImage
import com.paymentoptions.pos.ui.composables._components.images.qrpayment.QrPayment2
import com.paymentoptions.pos.ui.composables._components.images.qrpayment.QrPayment3
import com.paymentoptions.pos.ui.composables._components.images.qrpayment.QrPayment4
import com.paymentoptions.pos.ui.composables._components.images.qrpayment.QrPayment6
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.additionalcharge.AdditionalChargeBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.foodmenu.FoodMenuBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.reviewcart.ReviewCartBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens._flow.receivemoney.chargemoney.ChargeMoneyBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens.status.MessageForStatusScreen
import com.paymentoptions.pos.ui.composables.screens.status.StatusScreen
import com.paymentoptions.pos.ui.composables.screens.status.StatusScreenType
import com.paymentoptions.pos.ui.theme.noBorder
import com.paymentoptions.pos.utils.PaymentMethod
import com.paymentoptions.pos.utils.cashPaymentMethod
import com.paymentoptions.pos.utils.formatToPrecisionString
import com.paymentoptions.pos.utils.paymentMethods
import com.paymentoptions.pos.utils.qrCodePaymentMethod
import com.paymentoptions.pos.utils.tapPaymentMethod
import com.paymentoptions.pos.utils.viaLinkPaymentMethod

const val MAX_QUANTITY_PER_FOOD_ITEM = 20

@Composable
fun FoodOrderFlow(
    navController: NavController,
    initialFoodOrderFlowStage: FoodOrderFlowStage = FoodOrderFlowStage.MENU,
) {
    val context = LocalContext.current
    val activity = context as? Activity

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

    val scrollState = rememberScrollState()
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod>(paymentMethods.first()) }
    var nfcStatusPair by remember { mutableStateOf(Nfc.getStatus(context)) }
    var showDeveloperOptionsEnabled by remember { mutableStateOf(false) }
    var showNFCNotEnabled by remember { mutableStateOf(false) }

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
                        categoryId = selectedFoodCategory!!.CategoryID, newFoodItems
                    )

                } else cartState.replaceFoodCategory(
                    selectedFoodCategory!!.CategoryID, listOf<FoodItem>()
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Error fetching products from API", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        foodItemListAvailable = true
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
                updateFlowStage = { updateFlowStage(it) })
        }

        FoodOrderFlowStage.REVIEW_CART -> {
            SectionedLayout(
                navController = navController,
                bottomBarContent = BottomBarContent.TOGGLE_BUTTON,
                bottomSectionPaddingInDp = 0.dp,
                bottomSectionMinHeightRatio = 0.9f,
                enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent
            ) {
                ReviewCartBottomSectionContent(
                    navController,
                    enableScrolling = enableScrollingInsideBottomSectionContent,
                    cartState = cartState,
                    updateCartSate = { cartState = it.copy() },
                    updateFlowStage = { updateFlowStage(it) })
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
            val localEnableScrollingInsideBottomSectionContent = true

            SectionedLayout(
                navController = navController,
                bottomSectionMinHeightRatio = 0.35f,
                bottomSectionMaxHeightRatio = 0.35f,
                bottomBarContent = BottomBarContent.TOGGLE_BUTTON,
                bottomSectionPaddingInDp = 0.dp,
                enableScrollingOfBottomSectionContent = !localEnableScrollingInsideBottomSectionContent,
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
                                    title = "Error",
                                    text = "You need to disable developer options to proceed further.",
                                    acceptButtonText = "Exit",
                                    cancelButtonText = "Developer Options",
                                    onAcceptFn = {
                                        showDeveloperOptionsEnabled = false
                                        activity?.finish()
                                    },
                                    onDismissFn = {
                                        showDeveloperOptionsEnabled = false
                                        val intent =
                                            Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                                        context.startActivity(intent)
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
                                        .height(270.dp)
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
                                    modifier = Modifier.height(80.dp)
                                ) {
                                    VisaImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )

                                    MastercardImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )

                                    AmexImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )

                                    JcbImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
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
                                        .height(270.dp)
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

                                    QrPayment4(
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

                                    QrPayment6(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )
                                }

                                AssistChip(
                                    modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                                    onClick = { },
                                    label = {
                                        Text(
                                            "Ask customer to scan with GrabPay",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Light,
                                            color = Color.White
                                        )
                                    },
                                    border = noBorder,
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = Color.LightGray.copy(0.2f)
                                    ),
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.Lightbulb,
                                            contentDescription = "Hint",
                                            tint = Color.Yellow,
                                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                                        )
                                    })

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

                                Text(
                                    text = "Pay via link",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }) {
                ChargeMoneyBottomSectionContent(
                    navController,
                    enableScrolling = localEnableScrollingInsideBottomSectionContent,
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
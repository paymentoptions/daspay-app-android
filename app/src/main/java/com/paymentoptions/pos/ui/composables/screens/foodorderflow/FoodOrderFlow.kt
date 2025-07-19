package com.paymentoptions.pos.ui.composables.screens.foodorderflow

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.CategoryListDataRecord
import com.paymentoptions.pos.services.apiService.ProductListDataRecord
import com.paymentoptions.pos.services.apiService.endpoints.categoryList
import com.paymentoptions.pos.services.apiService.endpoints.productList
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout
import com.paymentoptions.pos.ui.composables.screens.foodorderflow.additionalcharge.AdditionalChargeBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens.foodorderflow.foodmenu.FoodMenuBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens.foodorderflow.reviewcart.ReviewCartBottomSectionContent

enum class FlowStage {
    MENU, REVIEW_CART, ADDITIONAL_CHARGE, PAYMENT, RESULT_PROCESSING, RESULT_ERROR, RESULT_SUCCESS
}

data class Cart(
    var foodItems: List<ProductListDataRecord> = listOf<ProductListDataRecord>(),
    var timestampInMilliseconds: Long? = null,
    var itemQuantity: Int = 0,
    var itemTotal: Float = 0.0f,
    var serviceChargePercentage: Float,
    var gstPercentage: Float,
    var additionalCharge: Float,
    var additionalAmountNote: String,
)

@Composable
fun FoodOrderFlow(
    navController: NavController,
    initialFlowStage: FlowStage = FlowStage.MENU,
) {
    val context = LocalContext.current
    val enableScrollingInsideBottomSectionContent = true

    var flowStage by remember { mutableStateOf<FlowStage>(initialFlowStage) }

    var foodCategoryList by remember { mutableStateOf<List<CategoryListDataRecord>>(listOf()) }
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

    fun updateFlowStage(newFlowStage: FlowStage) {
        flowStage = newFlowStage
    }

    fun updateCartState(newCart: Cart) {
        cartState = newCart
    }

    LaunchedEffect(Unit) {
        foodCategoryListAvailable = false
        try {

            val foodCategoryListFromAPI = categoryList(context)

            if (foodCategoryListFromAPI != null) foodCategoryList =
                foodCategoryList.plus(foodCategoryListFromAPI.data.records)


        } catch (e: Exception) {
            Toast.makeText(context, "Error fetching food categories from API", Toast.LENGTH_SHORT)
                .show()
        } finally {
            foodCategoryListAvailable = true
        }
    }

    LaunchedEffect(Unit) {
        foodItemListAvailable = false
        try {

            val foodItemListFromAPI = productList(context)

            if (foodItemListFromAPI != null) updateCartState(cartState.copy(foodItems = foodItemListFromAPI.data.records))

        } catch (e: Exception) {
            Toast.makeText(context, "Error fetching products from API", Toast.LENGTH_SHORT).show()
        } finally {
            foodItemListAvailable = true
        }
    }

    when (flowStage) {

        FlowStage.MENU -> SectionedLayout(
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
                foodItemsAvailable = foodItemListAvailable,
                cartState = cartState,
                updateCartState = { updateCartState(it) },
                updateFlowStage = { updateFlowStage(it) })
        }

        FlowStage.REVIEW_CART -> {
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
                    updateCartState = { updateCartState(it) },
                    updateFlowStage = { updateFlowStage(it) })
            }
        }

        FlowStage.ADDITIONAL_CHARGE -> {

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
                    updateCartState = { updateCartState(it) },
                    updateFlowStage = { updateFlowStage(it) })

            }
        }

        FlowStage.PAYMENT -> {}
        FlowStage.RESULT_PROCESSING -> {}
        FlowStage.RESULT_ERROR -> {}
        FlowStage.RESULT_SUCCESS -> {}
    }
}
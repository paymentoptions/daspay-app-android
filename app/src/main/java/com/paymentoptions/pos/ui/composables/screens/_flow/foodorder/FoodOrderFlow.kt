package com.paymentoptions.pos.ui.composables.screens._flow.foodorder

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
import co.yml.charts.common.extensions.isNotNull
import com.paymentoptions.pos.services.apiService.CategoryListDataRecord
import com.paymentoptions.pos.services.apiService.ProductListDataRecord
import com.paymentoptions.pos.services.apiService.endpoints.categoryList
import com.paymentoptions.pos.services.apiService.endpoints.productList
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.additionalcharge.AdditionalChargeBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.foodmenu.FoodMenuBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.reviewcart.ReviewCartBottomSectionContent

enum class FoodOrderFlowStage {
    MENU, REVIEW_CART, ADDITIONAL_CHARGE, PAYMENT, RESULT_PROCESSING, RESULT_ERROR, RESULT_SUCCESS
}

class FoodItem(
    val item: ProductListDataRecord,
    var cartQuantity: Int = 0,
) {
    fun decreaseQuantity() {
        this.cartQuantity--
    }

    fun increaseQuantity() {
        this.cartQuantity++
    }

    fun copyCartQuantity(f: FoodItem) {
        this.cartQuantity = f.cartQuantity
    }
}

class Cart(
    var foodItemMapByCategory: MutableMap<String, List<FoodItem>> = mutableMapOf<String, List<FoodItem>>(),
    var timestampInMilliseconds: Long? = null,
    var itemQuantity: Int = 0,
    var itemTotal: Float = 0.0f,
    var serviceChargePercentage: Float,
    var gstPercentage: Float,
    var additionalCharge: Float,
    var additionalAmountNote: String,
    var updateAvailable: Boolean = false,
) {

    fun decreaseFoodItemQuantity(foodItem: FoodItem) {
        if (foodItem.cartQuantity > 1) {
            foodItem.decreaseQuantity()
            this.itemQuantity--
            this.itemTotal -= foodItem.item.ProductPrice
        }

    }

    fun increaseFoodItemQuantity(foodItem: FoodItem) {
        if (foodItem.cartQuantity > 4) {
            foodItem.increaseQuantity()
            this.itemQuantity++
            this.itemTotal += foodItem.item.ProductPrice
        }
    }

    fun replaceFoodCategory(categoryId: String, newFoodItems: List<FoodItem>) {

        val oldFoodItemsInTheCategory = this.foodItemMapByCategory[categoryId]
        val newFoodItemsInTheCategorySorted =
            newFoodItems.sortedBy { foodItem -> foodItem.item.CategoryID }

        if (oldFoodItemsInTheCategory.isNullOrEmpty()) this.foodItemMapByCategory.plus(pair = categoryId to newFoodItems)
        else {

            newFoodItemsInTheCategorySorted.forEach { newFoodItem ->
                val oldFoodItem =
                    oldFoodItemsInTheCategory.find { oldFoodItem -> oldFoodItem.item.ProductID == newFoodItem.item.ProductID }

                if (oldFoodItem.isNotNull()) newFoodItem.copyCartQuantity(oldFoodItem!!)
            }

            this.foodItemMapByCategory[categoryId] = newFoodItemsInTheCategorySorted
        }
    }

    fun getFoodItems(): List<FoodItem> {

        var foodItems: List<FoodItem> = listOf<FoodItem>()

        this.foodItemMapByCategory.forEach { categoryFoodItemMap ->
            foodItems.plus(categoryFoodItemMap.value)
        }

        return foodItems.filter { foodItem -> foodItem.cartQuantity > 1 }
    }
}

@Composable
fun FoodOrderFlow(
    navController: NavController,
    initialFoodOrderFlowStage: FoodOrderFlowStage = FoodOrderFlowStage.MENU,
) {
    val context = LocalContext.current
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

    fun updateFlowStage(newFoodOrderFlowStage: FoodOrderFlowStage) {
        foodOrderFlowStage = newFoodOrderFlowStage
    }

//    fun updateCartState(newCart: Cart) {
//        cartState = newCart
//    }

    LaunchedEffect(Unit) {
        foodCategoryListAvailable = false
        try {
            val foodCategoryListFromAPI = categoryList(context)

            if (foodCategoryListFromAPI != null) foodCategoryList =
                foodCategoryListFromAPI.data.records

            selectedFoodCategory = foodCategoryList.firstOrNull()

        } catch (e: Exception) {
            Toast.makeText(context, "Error fetching food categories from API", Toast.LENGTH_SHORT)
                .show()
        } finally {
            foodCategoryListAvailable = true
        }
    }

    if (foodCategoryListAvailable) LaunchedEffect(selectedFoodCategory) {

        foodItemListAvailable = false

        if (selectedFoodCategory.isNotNull()) {
            try {
                val foodItemListFromAPI = productList(context, selectedFoodCategory!!.CategoryID)

                println("selectedFoodCategory: ${selectedFoodCategory!!.CategoryID} | $foodItemListFromAPI")

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
                    updateFlowStage = { updateFlowStage(it) })

            }
        }

        FoodOrderFlowStage.PAYMENT -> {}
        FoodOrderFlowStage.RESULT_PROCESSING -> {}
        FoodOrderFlowStage.RESULT_ERROR -> {}
        FoodOrderFlowStage.RESULT_SUCCESS -> {}
    }
}
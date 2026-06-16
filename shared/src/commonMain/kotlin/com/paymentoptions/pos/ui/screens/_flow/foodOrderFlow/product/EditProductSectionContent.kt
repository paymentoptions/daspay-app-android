package com.paymentoptions.pos.ui.screens._flow.foodOrderFlow.product

import com.paymentoptions.pos.decodeImageFromUri
import com.paymentoptions.pos.rememberKmpCameraLauncher
import com.paymentoptions.pos.rememberKmpFilePickerLauncher
import com.paymentoptions.pos.rememberKmpImagePickerLauncher
import com.paymentoptions.pos.showToast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.network.ProductRequest
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.Uri
import coil3.compose.AsyncImage
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.endpoints.editProduct
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.inputs.OutlinedTextInput
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.screens._flow.foodOrderFlow.FoodItem
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.enabledFilledButtonGradientBrush
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.utils.parseApiErrorMessage
import com.paymentoptions.pos.utils.formatToPrecisionString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
    ExperimentalComposeApi::class
)
@Composable
fun EditProductSectionContent(
    selectedFoodItem: FoodItem,
    updateFlowToMenu: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var productName = rememberTextFieldState(initialText = selectedFoodItem.item.ProductName)
    var productDescription = rememberTextFieldState(initialText = selectedFoodItem.item.ProductDesc?:"")
    var productPrice = rememberTextFieldState(initialText = selectedFoodItem.item.ProductPrice.formatToPrecisionString())
    var productCode = rememberTextFieldState(initialText = selectedFoodItem.item.ProductCode)
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showMediaSheet by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Multiplatform launchers
    val imagePickerLauncher = rememberKmpImagePickerLauncher { uri -> imageUri = uri }
    val cameraLauncher = rememberKmpCameraLauncher { uri -> imageUri = uri }
    val filePickerLauncher = rememberKmpFilePickerLauncher { uri -> imageUri = uri }

    if(isLoading){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Edit Item",
                    fontWeight = FontWeight.Bold,
                    style = AppTheme.typography.screenTitle.copy(fontSize = 24.sp),
                )

                IconButton(onClick = {
                    updateFlowToMenu()
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            // Product Name
            OutlinedTextInput(
                state = productName,
                placeholder = "",
                modifier = Modifier.fillMaxWidth(),
                label = "Product Name *",
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Product Description
            OutlinedTextInput(
                state = productDescription,
                placeholder = "",
                modifier = Modifier.fillMaxWidth(),
                label = "Product Description",
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Product Price
                Column(modifier = Modifier.weight(0.5f)) {
                    OutlinedTextInput(
                        state = productPrice,
                        label = "Product Price *",
                        placeholder =DPStorageManager.getTransactionCurrency(),
                        onlyDigits = true,
                        disabled = DPStorageManager.isStaff(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(modifier = Modifier.weight(0.5f)) {
                    OutlinedTextInput(
                        state = productCode,
                        label = "Product Code",
                        placeholder = "Ex. SKU-000",
                        disabled = DPStorageManager.isStaff(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Product Image
                Column(
                    modifier = Modifier.weight(0.5f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Product Image",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = purple50
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(

                        modifier = Modifier.fillMaxWidth()
                            .height(88.dp)
                            .border(
                                width = 1.5.dp,
                                color = Color(0xFF90CAF9),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { showMediaSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            if (imageUri != null) {
                            val bitmap = remember(imageUri) {
                                decodeImageFromUri(imageUri!!)
                            }

                            bitmap?.let {
                                Image(
                                    bitmap = it,
                                    contentDescription = null,
                                    modifier = Modifier.size(70.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            } else {
                                if (selectedFoodItem.item.ProductImage != null) {
                                    AsyncImage(
                                        model = selectedFoodItem.item.ProductImage,
                                        contentDescription = "",
                                        contentScale = ContentScale.Crop, // 🔥 IMPORTANT
                                        modifier = Modifier
                                            .size(50.dp)                  // 🔥 MUST be square
                                            .clip(CircleShape)            // 🔥 Clip AFTER size
                                            .border(borderThin, CircleShape)
                                        ,
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .background(
                                                brush = enabledFilledButtonGradientBrush,
                                                shape = RoundedCornerShape(6.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PhotoCamera,
                                            contentDescription = "Pick Image",
                                            tint = Color.White, // 👈 visible icon
                                            modifier = Modifier.size(50.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom sheet for media selection
            if (showMediaSheet) {
                ModalBottomSheet(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.White,
                    contentColor = primary500,
                    onDismissRequest = { showMediaSheet = false },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Select Media", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(

                        ) {
                            PickerButton(
                                text = "Camera",
                                icon = Icons.Default.PhotoCamera,
                                onClick = {
                                    showMediaSheet = false
                                    cameraLauncher.launch()
                                })
                            Spacer(modifier = Modifier.height(8.dp))
                            PickerButton(
                                text = "Gallery",
                                icon = Icons.Default.PhotoAlbum,
                                onClick = {
                                    showMediaSheet = false
                                    imagePickerLauncher.launch()
                                })
                            Spacer(modifier = Modifier.height(8.dp))
                            PickerButton(
                                text = "My Files",
                                icon = Icons.Default.FileOpen,
                                onClick = {
                                    showMediaSheet = false
                                    filePickerLauncher.launch("image/*")
                                })
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(10.dp))
            if (errorMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .background(
                            color = Color(0xFFEB5757).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Error,
                        contentDescription = "Error",
                        tint = Color(0xFFEB5757),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage.toString(),
                        color = Color(0xFFEB5757),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))

            FilledButton(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                text = "Edit Item",
                onClick = {
                    CoroutineScope(Dispatchers.Main).launch {
                        errorMessage = null
                        isLoading = true
                        // Validate
                        if (productName.text.toString()
                                .isBlank()
                        ) {
                            errorMessage = "Please fill all required fields."
                            isLoading = false
                            return@launch
                        }
                        // Check if productPrice.text.toString() is a valid float with up to 2 decimal places, else show error
                        val priceText = productPrice.text.toString().trim()

                        // Regex: integer OR decimal with up to 2 digits
                        val priceRegex = Regex("^\\d+(\\.\\d{1,2})?$")

                        if (priceText.isBlank() || !priceRegex.matches(priceText)) {
                            errorMessage = "Enter a valid price (up to 2 decimal places)."
                            isLoading = false
                            return@launch
                        }

                        // Safe conversion + rounding to 2 decimals
                        val priceFloat = priceText.toFloat()
                        val finalPrice = priceFloat.formatToPrecisionString().toFloat()
                        // val selectedFile = uriToTempFile(context, imageUri)

                        val result = try {
                            val addFoodResponse = editProduct(
                                request = getProductRequest(
                                    productName,
                                    productDescription,
                                    finalPrice,
                                    productCode,
                                    selectedFoodItem
                                ),
                                productId = selectedFoodItem.item.ProductID,
                                imageBytes = null, // Placeholder for now
                                imageFileName = null
                                )
                            addFoodResponse?.statusCode == 200L || addFoodResponse?.statusCode == 201L // success
                        } catch (e: Exception) {
                            errorMessage = parseApiErrorMessage(e, "Something went wrong..")
                            false
                        }
                        isLoading = false
                        if (result) {
                            withContext(Dispatchers.Main) {
                                showToast("Product edited successfully.")
                                updateFlowToMenu()
                            }
                        }
                    }
                }
            )
        }
    }
}

private fun getProductRequest(
    productName: TextFieldState,
    productDescription: TextFieldState,
    finalPrice: Float,
    productCode: TextFieldState,
    selectedFoodItem: FoodItem
): ProductRequest {
    if (DPStorageManager.isAdmin()) return ProductRequest(
        ProductName = productName.text.toString(),
        ProductDesc = productDescription.text.toString(),
        ProductPrice = finalPrice,
        ProductCode = productCode.text.toString(),
        ProductStatus = true,
        Currency = DPStorageManager.getTransactionCurrency(),
        MerchantID = selectedFoodItem.item.MerchantID,
        CategoryID = selectedFoodItem.item.CategoryID,
    ) else {
        return ProductRequest(
            ProductName = productName.text.toString(),
            ProductDesc = productDescription.text.toString(),
            ProductStatus = true,
            Currency = DPStorageManager.getTransactionCurrency(),
            MerchantID = selectedFoodItem.item.MerchantID,
            CategoryID = selectedFoodItem.item.CategoryID,
            ProductPrice = null,
            ProductCode = null,
        )

    }
}










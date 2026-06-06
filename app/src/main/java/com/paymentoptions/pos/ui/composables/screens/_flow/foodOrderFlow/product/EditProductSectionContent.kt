package com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.product

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.services.apiService.ProductRequest
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.paymentoptions.pos.device.DPSharedPreferences
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.endpoints.editProduct
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.inputs.OutlinedTextInput
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.FoodItem
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.enabledFilledButtonGradientBrush
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.utils.parseApiErrorMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var productName = rememberTextFieldState(initialText = selectedFoodItem.item.ProductName)
    var productDescription = rememberTextFieldState(initialText = selectedFoodItem.item.ProductDesc?:"")
    var productPrice = rememberTextFieldState(initialText = String.format(Locale.US, "%.2f", selectedFoodItem.item.ProductPrice))
    //var productType by remember { mutableStateOf(selectedFoodItem.item.ProductFoodType) }
    //var productSize by remember { mutableStateOf(selectedFoodItem.item.ProductSize) }
    var productCode = rememberTextFieldState(initialText = selectedFoodItem.item.ProductCode)
    //var productStock by remember { mutableStateOf(selectedFoodItem.item.ProductStock) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showMediaSheet by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Image picker launcher
    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri = uri
        }
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        // Handle camera result (imageUri should be set before launching)
        AppLogger.debug("It is called with tempCameraUri:$tempCameraUri and success : $success")
        if(success) {
            imageUri = tempCameraUri
        }
    }
    // File picker launcher
    val filePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri = uri
        }

    val cameraPermission = Manifest.permission.CAMERA
    var cameraPermissionRequested by remember { mutableStateOf(false) }
    val activity = LocalActivity.current

    fun launchCameraWithPermissionCheck(tempUri: Uri) {
        tempCameraUri = tempUri
        if (ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(tempUri)
        } else {
            cameraPermissionRequested = true
            ActivityCompat.requestPermissions(activity!!, arrayOf(cameraPermission), 1001)
        }
    }


    // Listen for permission result and launch camera if granted
    if (cameraPermissionRequested && ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED && tempCameraUri != null) {
        cameraLauncher.launch(tempCameraUri!!)
        cameraPermissionRequested = false
    }

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
                        placeholder =DPSharedPreferences.getTransactionCurrency(context),
                        onlyDigits = true,
                        disabled = DPSharedPreferences.isStaff(context),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(modifier = Modifier.weight(0.5f)) {
                    OutlinedTextInput(
                        state = productCode,
                        label = "Product Code",
                        placeholder = "Ex. SKU-000",
                        disabled = DPSharedPreferences.isStaff(context),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Product Type
//                Column(modifier = Modifier.weight(0.5f)) {
//                    Text(
//                        "Product Type",
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Medium,
//                        color = purple50
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    RectangularDropdownMenu(
//                        options = listOf("NONVEG", "VEG"),
//                        selected = productType,
//                        onSelected = { productType = it },
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                }
            }
//            Spacer(modifier = Modifier.height(8.dp))
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//
//                // Product Code
//                Column(modifier = Modifier.weight(0.5f)) {
//                    OutlinedTextInput(
//                        state = productCode,
//                        label = "Product Code * (Unique)",
//                        placeholder = "Ex. SKU-000",
//                        disabled = DPSharedPreferences.isStaff(context),
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                }
//
//                // Product Size
//                Column(modifier = Modifier.weight(0.5f)) {
//                    Text(
//                        "Product Size",
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Medium,
//                        color = purple50
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    RectangularDropdownMenu(
//                        options = listOf("REGULAR", "LARGE", "SMALL"),
//                        selected = productSize,
//                        onSelected = { productSize = it },
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                }
//            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Product Stock
//                Column(modifier = Modifier.weight(0.5f)) {
//                    Text(
//                        "Product Stock",
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Medium,
//                        color = purple50
//                    )
//                    Spacer(modifier = Modifier.height(6.dp))
//
//                    Box(
//                        modifier = Modifier.fillMaxWidth()
//                            .height(88.dp)
//                            .border(
//                                width = 1.5.dp,
//                                color = Color(0xFF90CAF9),
//                                shape = RoundedCornerShape(6.dp)
//                            )
//                            .background(
//                                color = Color.White,
//                                shape = RoundedCornerShape(6.dp)
//                            )
//                            .padding(horizontal = 12.dp, vertical = 4.dp),
//                    ) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.Center,
//                            modifier = Modifier.padding(20.dp)
//                                .height(70.dp)
//                                .background(
//                                    brush = enabledFilledButtonGradientBrush,
//                                    shape = RoundedCornerShape(20.dp)
//                                )
//                                .border(
//                                    shape = RoundedCornerShape(20.dp),
//                                    width = 1.dp,
//                                    color = Color.Blue
//                                )
//                        ) {
//                            IconButton(
//                                onClick = { if (productStock > 1) productStock-- },
//                                modifier = Modifier.size(40.dp)
//                            ) {
//                                Icon(Icons.Default.Remove,
//                                    contentDescription = "Decrease",
//                                    tint = Color.White)
//                            }
//
//                            Text(
//                                productStock.toString(),
//                                fontWeight = FontWeight.Bold,
//                                fontSize = 14.sp,
//                                color = Color.White,
//                                modifier = Modifier.padding(horizontal = 12.dp)
//                            )
//
//                            IconButton(
//                                onClick = { productStock++ },
//                                modifier = Modifier.size(40.dp)
//                            ) {
//                                Icon(Icons.Default.Add,
//                                    contentDescription = "Increase",
//                                    tint = Color.White)
//                            }
//                        }
//                    }
//                }

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
                                    context.contentResolver
                                        .openInputStream(imageUri!!)
                                        ?.use { BitmapFactory.decodeStream(it) }
                                }

                                bitmap?.let {
                                    Image(
                                        bitmap = it.asImageBitmap(),
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
                    // shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
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
                                    val tempFile = File(
                                        context.cacheDir,
                                        "camera_${System.currentTimeMillis()}.jpg"
                                    )
                                    val tempUri = FileProvider.getUriForFile(
                                        context,
                                        context.packageName + ".fileprovider",
                                        tempFile
                                    )
                                    launchCameraWithPermissionCheck(tempUri)
                                })
                            Spacer(modifier = Modifier.height(8.dp))
                            PickerButton(
                                text = "Gallery",
                                icon = Icons.Default.PhotoAlbum,
                                onClick = {
                                    showMediaSheet = false
                                    imagePickerLauncher.launch("image/*")
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
                    CoroutineScope(Dispatchers.IO).launch {
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
                        val finalPrice = String.format(Locale.US,"%.2f", priceFloat).toFloat()
                        val selectedFile = uriToTempFile(context, imageUri)

                        val result = try {
                            val addFoodResponse = editProduct(
                                request = getProductRequest(
                                    context,
                                    productName,
                                    productDescription,
                                    finalPrice,
//                                    productType,
//                                    productSize,
                                    productCode,
                                   // productStock,
                                    selectedFoodItem
                                ),
                                productId = selectedFoodItem.item.ProductID,
                                imageBytes = selectedFile?.readBytes(),
                                imageFileName = selectedFile?.absolutePath
                                )
                            addFoodResponse?.statusCode == 200L || addFoodResponse?.statusCode == 201L // success
                        } catch (e: Exception) {
                            errorMessage = parseApiErrorMessage(e, "Something went wrong..")
                            false
                        }
                        isLoading = false
                        if (result) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "Product edited successfully.",
                                    Toast.LENGTH_SHORT
                                ).show()
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
    context: Context,
    productName: TextFieldState,
    productDescription: TextFieldState,
    finalPrice: Float,
//    productType: String,
//    productSize: String,
    productCode: TextFieldState,
    //productStock: Int,
    selectedFoodItem: FoodItem
): ProductRequest {
    if (DPSharedPreferences.isAdmin(context)) return ProductRequest(
        ProductName = productName.text.toString(),
        ProductDesc = productDescription.text.toString(),
        ProductPrice = finalPrice,
//        ProductFoodType = productType,
//        ProductSize = productSize,
        ProductCode = productCode.text.toString(),
       // ProductStock = productStock.toLong(),
        ProductStatus = true,
        Currency = DPSharedPreferences.getTransactionCurrency(context),
        MerchantID = selectedFoodItem.item.MerchantID,
        CategoryID = selectedFoodItem.item.CategoryID,
    ) else {
        return ProductRequest(
            ProductName = productName.text.toString(),
            ProductDesc = productDescription.text.toString(),
//            ProductFoodType = productType,
//            ProductSize = productSize,
//            ProductStock = productStock.toLong(),
            ProductStatus = true,
            Currency = DPSharedPreferences.getTransactionCurrency(context),
            MerchantID = selectedFoodItem.item.MerchantID,
            CategoryID = selectedFoodItem.item.CategoryID,
            ProductPrice = null,
            ProductCode = null,
        )

    }
}









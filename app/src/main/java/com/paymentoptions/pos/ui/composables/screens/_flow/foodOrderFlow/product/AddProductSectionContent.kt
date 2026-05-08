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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.services.apiService.ProductRequest
import com.paymentoptions.pos.services.apiService.CategoryListDataRecord
import com.paymentoptions.pos.services.apiService.endpoints.addProduct
import com.paymentoptions.pos.ui.composables._components.inputs.BasicTextInput
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Remove
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.purple50
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import androidx.core.content.FileProvider
import com.paymentoptions.pos.BuildConfig
import com.paymentoptions.pos.device.DPSharedPreferences
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.ui.composables._components.inputs.OutlinedTextInput
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.disabledFilledButtonGradientBrush
import com.paymentoptions.pos.ui.theme.enabledFilledButtonGradientBrush
import com.paymentoptions.pos.ui.theme.innerShadow
import com.paymentoptions.pos.ui.theme.shadowColor
import com.paymentoptions.pos.ui.theme.shadowColor2
import com.paymentoptions.pos.utils.modifiers.innerShadow
import java.util.Locale

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
    ExperimentalComposeApi::class
)
@Composable
fun AddProductSectionContent(
    categorySelected: CategoryListDataRecord?,
    updateFlowToMenu: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val productName = rememberTextFieldState()
    val productDescription = rememberTextFieldState()
    val productPrice = rememberTextFieldState()
    var productType by remember { mutableStateOf("NONVEG") }
    var productSize by remember { mutableStateOf("REGULAR") }
    val productCode = rememberTextFieldState()
    var productStock by remember { mutableStateOf(1) }
    var showMediaSheet by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Image picker launcher
    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri = uri
        }
    // Camera launcher
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            // Handle camera result (imageUri should be set before launching)
            AppLogger.debug("It is called with tempCameraUri:$tempCameraUri and success : $success")
            if (success) {
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
        if (ContextCompat.checkSelfPermission(
                context,
                cameraPermission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            cameraLauncher.launch(tempUri)
        } else {
            cameraPermissionRequested = true
            ActivityCompat.requestPermissions(activity!!, arrayOf(cameraPermission), 1001)
        }
    }

    // Listen for permission result and launch camera if granted
    if (cameraPermissionRequested && ContextCompat.checkSelfPermission(
            context,
            cameraPermission
        ) == PackageManager.PERMISSION_GRANTED && tempCameraUri != null
    ) {
        cameraLauncher.launch(tempCameraUri!!)
        cameraPermissionRequested = false
    }

    if (isLoading) {
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
                    text = "Add New Item",
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
                placeholder = "Enter Name",
                modifier = Modifier.fillMaxWidth(),
                label = "Product Name *",
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Product Description
            OutlinedTextInput(
                state = productDescription,
                placeholder = "Enter Description",
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
                        placeholder = "Enter Price",
                        modifier = Modifier.fillMaxWidth(),
                        label = "Product Price *",
                        onlyDigits = true,
                    )
                }

                // Product Type
                Column(modifier = Modifier.weight(0.5f)) {
                    Text(
                        "Product Type",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = purple50
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    RectangularDropdownMenu(
                        options = listOf("NONVEG", "VEG"),
                        selected = productType,
                        onSelected = { productType = it },
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

                // Product Code
                Column(modifier = Modifier.weight(0.5f)) {
                    OutlinedTextInput(
                        state = productCode,
                        placeholder = "Enter Unique Product code",
                        modifier = Modifier.fillMaxWidth(),
                        label = "Product Code * (Unique)",
                    )
                }

                // Product Size
                Column(modifier = Modifier.weight(0.5f)) {
                    Text(
                        "Product Size",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = purple50
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    RectangularDropdownMenu(
                        options = listOf("REGULAR", "LARGE", "SMALL"),
                        selected = productSize,
                        onSelected = { productSize = it },
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

                // Product Stock
                Column(modifier = Modifier.weight(0.5f)) {
                    Text(
                        "Product Stock",
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
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(20.dp)
                                .height(70.dp)
                                .background(
                                    brush = enabledFilledButtonGradientBrush,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .border(
                                    shape = RoundedCornerShape(20.dp),
                                    width = 1.dp,
                                    color = Color.Blue
                                )
                        ) {
                            IconButton(
                                onClick = { if (productStock > 1) productStock-- },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    tint = Color.White)
                            }

                            Text(
                                productStock.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            IconButton(
                                onClick = { productStock++ },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.Add,
                                    contentDescription = "Increase",
                                    tint = Color.White)
                            }
                        }
                    }
                }

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

            Spacer(modifier = Modifier.height(15.dp))

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

            Spacer(modifier = Modifier.height(15.dp))

            FilledButton(
                modifier = Modifier
                    .fillMaxWidth(),
                text = "Add Item",
                onClick = {
                    isLoading = true
                    CoroutineScope(Dispatchers.IO).launch {
                        errorMessage = null
                        // Validate
                        if (productName.text.toString().isBlank() || productCode.text.toString()
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
                        val finalPrice = String.format(Locale.US, "%.2f", priceFloat).toFloat()


                        val result = try {
                            AppLogger.debug("categorySelected id: $categorySelected")

                            val addFoodResponse = addProduct(
                                context = context, request = ProductRequest(
                                    ProductName = productName.text.toString(),
                                    ProductDesc = productDescription.text.toString(),
                                    ProductPrice = finalPrice,
                                    ProductFoodType = productType,
                                    ProductSize = productSize,
                                    ProductCode = productCode.text.toString(),
                                    ProductStock = productStock.toLong(),
                                    ProductStatus = true,
                                    Currency = DPSharedPreferences.getTransactionCurrency(context),
                                    MerchantID = categorySelected?.MerchantID ?: "",
                                    CategoryID = categorySelected?.CategoryID ?: "",
                                ),
                                selectedFile = uriToTempFile(context, imageUri)
                            )
                            addFoodResponse?.statusCode == 200L || addFoodResponse?.statusCode == 201L // success
                        } catch (e: retrofit2.HttpException) {
                            errorMessage = "Something went wrong.."
                            AppLogger.error("add product HTTP error ${e.code()}: ${e.message()}")
                            false
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to add product."
                            false
                        }
                        isLoading = false
                        if (result) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "Product added successfully!",
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

@Composable
fun PickerButton(
    text: String, icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .clickable { onClick() }
            .width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(36.dp),
            tint = Color(0xFF90CAF9)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF90CAF9),
            fontWeight = FontWeight.Medium
        )
    }
}


fun uriToTempFile(context: Context, uri: Uri?): File? {
    if (uri == null) return null
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        FileOutputStream(tempFile).use { output ->
            inputStream?.copyTo(output)
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RectangularDropdownMenu(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var buttonWidth by remember { mutableStateOf(0) }

    val borderColor = Color(0xFF90CAF9)

    Box(modifier = modifier
    ) {

        // 🔹 Dropdown button
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            border = BorderStroke(1.dp, borderColor.copy(alpha = 0.5f)),

//            colors = ButtonDefaults.outlinedButtonColors(
//                contentColor = MaterialTheme.colorScheme.onSurface
//            ),
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { buttonWidth = it.width }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = selected, color = purple50)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = borderColor
                )
            }
        }

        // 🔹 Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .width(with(LocalDensity.current) { buttonWidth.toDp() })
                .border(1.dp, borderColor, RoundedCornerShape(4.dp))
//                .background(shadowColor2.copy(alpha = 0.5f))
                .background(color = Color.White)
                .drawBehind {
                    // fade bottom 5.dp to transparent
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(0f, size.height - 25.dp.toPx()),
                        size = Size(size.width, 10.dp.toPx()),
                        blendMode = BlendMode.Darken // clears pixels, making them transparent
                    )
                }
                .innerShadow(
                    color = innerShadow,
                    blur = 10.dp,
                    spread = 5.dp,
                    cornersRadius = 0.dp,
                    offsetX = 0.dp,
                    offsetY = 0.dp,
                    showBottom = false
                ),
        ) {
            options.forEachIndexed { index, option ->

                DropdownMenuItem(
                    text = { Text(option, color = primary500) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                )

                // 🔹 Divider between items (except last)
                if (index < options.lastIndex) {
                    Divider(
                        color = borderColor,
                        thickness = 0.8.dp
                    )
                }
            }
        }
    }
}

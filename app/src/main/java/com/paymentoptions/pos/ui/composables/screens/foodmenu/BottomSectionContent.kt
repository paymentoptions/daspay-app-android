package com.paymentoptions.pos.ui.composables.screens.foodmenu

import CustomDialog
import android.app.Activity
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.ClientHeadlessImpl
import com.paymentoptions.pos.device.DeveloperOptions
import com.paymentoptions.pos.device.Nfc
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.Address
import com.paymentoptions.pos.services.apiService.PaymentMethod
import com.paymentoptions.pos.services.apiService.PaymentRequest
import com.paymentoptions.pos.services.apiService.PaymentResponse
import com.paymentoptions.pos.services.apiService.PaymentReturnUrl
import com.paymentoptions.pos.services.apiService.endpoints.payment
import com.paymentoptions.pos.ui.composables._components.CustomCircularProgressIndicator
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.theme.DisabledButtonColor
import com.paymentoptions.pos.ui.theme.Orange10
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.decodeJwtPayload
import com.paymentoptions.pos.utils.getDasmidFromToken
import com.paymentoptions.pos.utils.getDeviceIpAddress
import com.paymentoptions.pos.utils.getDeviceTimeZone
import com.paymentoptions.pos.utils.getKeyFromToken
import com.theminesec.lib.dto.common.Amount
import com.theminesec.lib.dto.poi.PoiRequest
import com.theminesec.lib.dto.transaction.TranType
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.model.WrappedResult
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Currency
import java.util.UUID

@Composable
fun BottomSectionContent(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)

    ) {
        Text(
            text = "Food Menu Screen Content",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = primary900,
        )

    }
}
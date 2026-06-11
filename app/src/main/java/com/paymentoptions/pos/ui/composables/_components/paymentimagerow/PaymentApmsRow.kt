package com.paymentoptions.pos.ui.composables._components.paymentimagerow

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.paymentoptions.pos.device.DPStorageManager.getApms
import com.paymentoptions.pos.ui.composables._components.images.apms.AliPayImage
import com.paymentoptions.pos.ui.composables._components.images.apms.ApplePayImage
import com.paymentoptions.pos.ui.composables._components.images.apms.DinersClubPayImage
import com.paymentoptions.pos.ui.composables._components.images.apms.GooglePayImage
import com.paymentoptions.pos.ui.composables._components.images.apms.GsCashPayImage
import com.paymentoptions.pos.ui.composables._components.images.apms.KonbibniPayImage
import com.paymentoptions.pos.ui.composables._components.images.apms.PayEasyImage
import com.paymentoptions.pos.ui.composables._components.images.apms.PayPayImage
import com.paymentoptions.pos.ui.composables._components.images.apms.WechatPayImage


@Composable
fun PaymentApmsRow(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var apms by remember { mutableStateOf(getApms()) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.horizontalScroll(state = rememberScrollState())
    ) {

        if (apms.hasAlipay) AliPayImage()
        if (apms.hasApplePay) ApplePayImage()
        if (apms.hasDinersClub) DinersClubPayImage()
        if (apms.hasGooglePay) GooglePayImage()
        if (apms.hasGCash) GsCashPayImage()
        if (apms.hasKonbini) KonbibniPayImage()
        if (apms.hasPayEasy) PayEasyImage()
        if (apms.hasPayPay) PayPayImage()
        if (apms.hasWechatpay) WechatPayImage()
    }
}
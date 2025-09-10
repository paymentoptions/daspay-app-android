package com.paymentoptions.pos.ui.composables._components.paymentimagerow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.paymentoptions.pos.device.getApms
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
    var apms by remember { mutableStateOf(getApms(context)) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {


        if (apms.hasAlipay) AliPayImage(
            modifier = Modifier
                .fillMaxHeight()
                .clip(shape = RoundedCornerShape(16.dp))
                .weight(1f)
        )

        if (apms.hasApplePay) ApplePayImage(
            modifier = Modifier
                .fillMaxHeight()
                .clip(shape = RoundedCornerShape(16.dp))
                .weight(1f)
        )

        if (apms.hasDinersClub) DinersClubPayImage(
            modifier = Modifier
                .fillMaxHeight()
                .clip(shape = RoundedCornerShape(16.dp))
                .weight(1f)
        )

        if (apms.hasGooglePay) GooglePayImage(
            modifier = Modifier
                .fillMaxHeight()
                .clip(shape = RoundedCornerShape(16.dp))
                .weight(1f)
        )

        if (apms.hasGCash) GsCashPayImage(
            modifier = Modifier
                .fillMaxHeight()
                .clip(shape = RoundedCornerShape(16.dp))
                .weight(1f)
        )

        if (apms.hasKonbini) KonbibniPayImage(
            modifier = Modifier
                .fillMaxHeight()
                .clip(shape = RoundedCornerShape(16.dp))
                .weight(1f)
        )

        if (apms.hasPayEasy) PayEasyImage(
            modifier = Modifier
                .fillMaxHeight()
                .clip(shape = RoundedCornerShape(16.dp))
                .weight(1f)
        )

        if (apms.hasPayPay) PayPayImage(
            modifier = Modifier
                .fillMaxHeight()
                .clip(shape = RoundedCornerShape(16.dp))
                .weight(1f)
        )

        if (apms.hasWechatpay) WechatPayImage(
            modifier = Modifier
                .fillMaxHeight()
                .clip(shape = RoundedCornerShape(16.dp))
                .weight(1f)
        )
    }
}
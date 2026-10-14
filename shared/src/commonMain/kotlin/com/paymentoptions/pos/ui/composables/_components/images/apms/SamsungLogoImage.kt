package com.paymentoptions.pos.ui.composables._components.images.apms

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.alipay_apms
import paymentoptionspos.shared.generated.resources.samsungpay_apms

@Composable
fun SamsungLogoImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.samsungpay_apms),
        contentDescription = "AliPay",
        modifier = modifier
    )
}
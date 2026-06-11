package com.paymentoptions.pos.ui.composables._components.paymentimagerow

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.ui.composables._components.images.schemes.AmexImage
import com.paymentoptions.pos.ui.composables._components.images.schemes.JcbImage
import com.paymentoptions.pos.ui.composables._components.images.schemes.MastercardImage
import com.paymentoptions.pos.ui.composables._components.images.schemes.UnionPayImage
import com.paymentoptions.pos.ui.composables._components.images.schemes.VisaImage

@Composable
fun PaymentSchemesRow(modifier: Modifier = Modifier) {
    var schemes by remember { mutableStateOf(DPStorageManager.getSchemes()) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.horizontalScroll(state = rememberScrollState())
    ) {
        if (schemes.hasVISA) VisaImage(
            modifier = Modifier
                .fillMaxHeight()
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .padding(end = 5.dp)
        )

        if (schemes.hasMastercard) MastercardImage(
            modifier = Modifier
                .fillMaxHeight()
                .background(Color.White, shape = RoundedCornerShape(8.dp))
        )

        if (schemes.hasAmex) AmexImage(
            modifier = Modifier
                .fillMaxHeight()
                .background(Color.White, shape = RoundedCornerShape(8.dp))
        )

        if (schemes.hasJCB) JcbImage(
            modifier = Modifier
                .fillMaxHeight()
                .background(Color.White, shape = RoundedCornerShape(8.dp))
        )

        if (schemes.hasUnionPay) {
            UnionPayImage(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
            )
        }
    }
}

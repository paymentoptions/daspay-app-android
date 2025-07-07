package com.paymentoptions.pos.ui.composables._components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.theme.red500

@Composable
fun NoData(
    modifier: Modifier = Modifier,
    text: String = "No Data",
    fontSize: TextUnit = 12.sp,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text(text = text, color = red500.copy(alpha = 0.5f), fontSize = fontSize)
    }
}
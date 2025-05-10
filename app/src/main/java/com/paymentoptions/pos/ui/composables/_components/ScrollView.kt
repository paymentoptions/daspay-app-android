package com.paymentoptions.pos.ui.composables._components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScrollView(text: String) {

    val scrollState = rememberScrollState()

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .height(200.dp)
            .padding(20.dp)
            .verticalScroll(scrollState)
    ) {
        Text(text, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }

}


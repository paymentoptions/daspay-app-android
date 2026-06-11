package com.paymentoptions.pos.ui.composables._components.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class Email(
    val id: String = "",
    val subject: String = "Shared via DASPay",
    val text: String,
)

@Composable
expect fun EmailButton(text: String, email: Email, modifier: Modifier = Modifier)

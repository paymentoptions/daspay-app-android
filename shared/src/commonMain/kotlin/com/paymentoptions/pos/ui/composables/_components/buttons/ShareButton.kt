package com.paymentoptions.pos.ui.composables._components.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun ShareButton(text: String, shareContent: String, modifier: Modifier = Modifier)

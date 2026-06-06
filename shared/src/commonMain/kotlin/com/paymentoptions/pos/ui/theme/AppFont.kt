package com.paymentoptions.pos.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.inter_bold
import paymentoptionspos.shared.generated.resources.inter_extralight
import paymentoptionspos.shared.generated.resources.inter_light
import paymentoptionspos.shared.generated.resources.inter_medium
import paymentoptionspos.shared.generated.resources.inter_regular
import paymentoptionspos.shared.generated.resources.inter_semibold
import org.jetbrains.compose.resources.Font

/**
 * Inter font family loaded from shared Compose resources.
 *
 * NOTE: [FontFamily] using Compose Resources must be obtained inside a
 * [Composable] function. Pass the result down the tree or store it as a
 * CompositionLocal (see [AppTheme]).
 */
@Composable
fun interFontFamily() = FontFamily(
    Font(Res.font.inter_extralight, FontWeight.ExtraLight),
    Font(Res.font.inter_light,      FontWeight.Light),
    Font(Res.font.inter_regular,    FontWeight.Normal),
    Font(Res.font.inter_medium,     FontWeight.Medium),
    Font(Res.font.inter_semibold,   FontWeight.SemiBold),
    Font(Res.font.inter_bold,       FontWeight.Bold),
)

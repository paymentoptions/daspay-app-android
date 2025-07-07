package com.paymentoptions.pos.ui.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val darkColorScheme = AppColorScheme(
    background = Color.Black,
    onBackground = primary500,
    primary = primary500,
    onPrimary = primary100,
    secondary = primary900,
    onSecondary = primary200,
)

private val lightColorScheme = AppColorScheme(
    background = Color.White,
    onBackground = primary500,
    primary = primary500,
    onPrimary = primary100,
    secondary = primary900,
    onSecondary = primary200,
)

private val typography = AppTypography(
    screenTitle = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        brush = textGradientBrush
    ),
    titleNormal = TextStyle(
        fontFamily = Inter,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = primary900,
    ),
    footnote = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = purple50
    ),

    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        color = primary500,
    ),

    body = TextStyle(
        fontFamily = Inter, fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = primary900
    ),
    labelNormal = TextStyle(
        fontFamily = Inter, fontSize = 14.sp, color = purple50
    ),
    labelSmall = TextStyle(
        fontFamily = Inter, fontSize = 12.sp
    ),

    )

private val shape = AppShape(
    container = RoundedCornerShape(12.dp),
    button = RoundedCornerShape(50),
)

private val size = AppSize(
    extraLarge = 24.dp,
    large = 20.dp,
    medium = 16.dp,
    normal = 12.dp,
    small = 8.dp,
    extraSmall = 4.dp
)

@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (isDarkTheme) darkColorScheme else lightColorScheme
    val rippleIndication = ripple()
//
//    val view = LocalView.current
//    if (!view.isInEditMode) {
//        SideEffect {
//            val window = (view.context as Activity).window
//            window.statusBarColor = primary500.toArgb()
//            WindowCompat
//                .getInsetsController(window, view)
//                .isAppearanceLightStatusBars = isDarkTheme
//        }
//    }

    CompositionLocalProvider(
        LocalAppColorScheme provides colorScheme,
        LocalAppTypography provides typography,
        LocalAppShape provides shape,
        LocalAppSize provides size,
        LocalIndication provides rippleIndication,
        content = content
    )
}

object AppTheme {

    val colorScheme: AppColorScheme
        @Composable get() = LocalAppColorScheme.current

    val typography: AppTypography
        @Composable get() = LocalAppTypography.current

    val shape: AppShape
        @Composable get() = LocalAppShape.current

    val size: AppSize
        @Composable get() = LocalAppSize.current
}
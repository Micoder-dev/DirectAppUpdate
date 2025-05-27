package com.micoder.directappupdate.components

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object GoldenTheme {
    val primary = Color(0xFFD4AF37)
    val primaryVariant = Color(0xFFB8860B)
    val secondary = Color(0xFFDFBD69)
    val background = Color(0xFF1A1A1A)
    val surface = Color(0xFF2D2D2D)
    val onPrimary = Color(0xFF000000)
    val onSecondary = Color(0xFF000000)
    val onBackground = Color(0xFFFFFFFF)
    val onSurface = Color(0xFFFFFFFF)
    val outline = Color(0xFFDAA520)

    val goldHorizontalGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFE291),
            Color(0xFFFF9F2F)
        )
    )

    val goldHorizontalGradient2 = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFD8C18F),
            Color(0xFF9B7442)
        )
    )

    val darkGoldGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFDEB765),
            Color(0xFF9B7442)
        )
    )

    val horizontalGoldGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFD8C18F),
            Color(0xFF9B7442)
        )
    )

    val goldLayerTextColor = Color(0xFF382308)

    val bluishWhiteTextColor = Color(0xFFE0F7FA)

    val horizontalLiveGoldGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFE291),
            Color(0xFFFF9F2F)
        )
    )

}
package com.mobileprism.fishing.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class CustomColors(
    val secondaryTextColor: Color,
    val secondaryIconColor: Color,
    val backgroundSecondaryColor: Color,
)

fun darkCustomColors(
    secondaryTextColor: Color = Color(0xFFB0B0B8),
    secondaryIconColor: Color = Color(0xFF9E9EA6),
    backgroundSecondaryColor: Color = secondaryBlueLightColorTransparent
): CustomColors = CustomColors(
    secondaryTextColor,
    secondaryIconColor,
    backgroundSecondaryColor
)

fun lightCustomColors(
    secondaryTextColor: Color = secondaryFigmaTextColor,
    secondaryIconColor: Color = Color(0xFF6E6E76),
    backgroundSecondaryColor: Color = secondaryBlueLightColorTransparent
): CustomColors = CustomColors(
    secondaryTextColor,
    secondaryIconColor,
    backgroundSecondaryColor
)

val LocalColors = compositionLocalOf { lightCustomColors() }

val MaterialTheme.customColors: CustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalColors.current

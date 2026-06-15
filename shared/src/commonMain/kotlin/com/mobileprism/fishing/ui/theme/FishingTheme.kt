package com.mobileprism.fishing.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

object FishingTheme {
    val colorScheme: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes

    val customColors: CustomColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    val spacing: Spacing get() = Spacing
    val iconSize: IconSize get() = IconSize
    val emphasis: Emphasis get() = Emphasis
    val elevation: Elevation get() = Elevation
}

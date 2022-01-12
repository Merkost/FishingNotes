package com.joesemper.fishing.compose.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class CustomColors(
    val secondaryTextColor: Color,
    val secondaryIconColor: Color,

    )

fun darkCustomColors(
    secondaryTextColor: Color = Color.LightGray,
    secondaryIconColor: Color = Color.Gray,

): CustomColors = CustomColors(
    secondaryTextColor,
    secondaryIconColor,


)

fun lightCustomColors(
    secondaryTextColor: Color = secondaryFigmaTextColor,
    secondaryIconColor: Color = Color.Gray,
    ): CustomColors = CustomColors(
    secondaryTextColor,
    secondaryIconColor,


)

val LocalColors = compositionLocalOf { lightCustomColors() }

val MaterialTheme.customColors: CustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalColors.current

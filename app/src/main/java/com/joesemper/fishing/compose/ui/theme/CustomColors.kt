package com.joesemper.fishing.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class CustomColors(
    val secondary: Color

)

fun darkCustomColors(
    secondary: Color = Color.Black,

) : CustomColors = CustomColors(secondary)

fun lightCustomColors(
    secondary: Color = Color.White,

) : CustomColors = CustomColors(secondary)

val LocalColors = compositionLocalOf { lightCustomColors() }

val MaterialTheme.customColors: CustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalColors.current

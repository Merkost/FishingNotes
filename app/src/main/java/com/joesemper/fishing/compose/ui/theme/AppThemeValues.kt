package com.joesemper.fishing.compose.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color


enum class AppThemeValues(val color: Color) {
    Blue(primaryBlueColor),
    Green(primaryFigmaColor);



}

fun getAppThemeValueFromColor(color: Color): AppThemeValues {
    return when(color) {
        AppThemeValues.Blue.color -> AppThemeValues.Blue
        AppThemeValues.Green.color -> AppThemeValues.Green
        else -> AppThemeValues.Blue
    };
}

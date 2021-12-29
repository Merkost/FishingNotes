package com.joesemper.fishing.compose.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color


enum class AppThemeValues(val color: Color) {
    Blue(primaryBlueColor),
    Green(primaryFigmaColor);

    fun getColor(selectedColor: Color): AppThemeValues {
        return when (selectedColor) {
            Blue.color -> Blue
            Green.color -> Green
            else -> Blue
        }
    }
}

package com.mobileprism.fishing.ui.utils.enums

import androidx.compose.ui.graphics.Color
import com.mobileprism.fishing.ui.theme.primaryBlueColor
import com.mobileprism.fishing.ui.theme.primaryFigmaColor


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

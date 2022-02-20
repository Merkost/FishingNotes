package com.mobileprism.fishing.compose.ui.utils.enums

import androidx.compose.ui.graphics.Color
import com.mobileprism.fishing.compose.ui.theme.primaryBlueColor
import com.mobileprism.fishing.compose.ui.theme.primaryFigmaColor


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

package com.mobileprism.fishing.ui.utils.enums

import androidx.compose.ui.graphics.Color
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.theme.primaryBlueColor
import com.mobileprism.fishing.ui.theme.primaryFigmaColor

enum class AppThemeValues(val color: Color?, val titleRes: Int) {
    Dynamic(null, R.string.theme_dynamic),
    Blue(primaryBlueColor, R.string.theme_blue),
    Green(primaryFigmaColor, R.string.theme_green);

    companion object {
        fun getTheme(selectedColor: Color?): AppThemeValues {
            return when (selectedColor) {
                null -> Dynamic
                Blue.color -> Blue
                Green.color -> Green
                else -> Blue
            }
        }
    }
}

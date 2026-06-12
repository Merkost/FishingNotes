package com.mobileprism.fishing.ui.utils.enums

import androidx.compose.ui.graphics.Color
import com.mobileprism.fishing.ui.theme.primaryBlueColor
import com.mobileprism.fishing.ui.theme.primaryFigmaColor
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class AppThemeValues(val color: Color?, val titleRes: StringResource) {
    Dynamic(null, Res.string.theme_dynamic),
    Blue(primaryBlueColor, Res.string.theme_blue),
    Green(primaryFigmaColor, Res.string.theme_green);

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

package com.mobileprism.fishing.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import com.mobileprism.fishing.ui.utils.enums.AppThemeValues

@Composable
expect fun FishingNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
)

@Composable
expect fun isAppInDarkTheme(): Boolean

@Composable
expect fun resolveDynamicColorScheme(darkTheme: Boolean): ColorScheme?

@Composable
fun chooseColorScheme(appTheme: AppThemeValues?, darkTheme: Boolean): ColorScheme {
    return when (appTheme) {
        AppThemeValues.Dynamic -> resolveDynamicColorScheme(darkTheme)
            ?: if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
        AppThemeValues.Blue -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
        AppThemeValues.Green -> if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
        else -> InitColorScheme
    }
}

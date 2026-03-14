package com.mobileprism.fishing.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.utils.enums.DarkModeValues
import org.koin.compose.koinInject

@Composable
actual fun FishingNotesTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val userPreferences: UserPreferences = koinInject()
    val appTheme = userPreferences.appTheme.collectAsState(null)
    val darkModePreference = userPreferences.darkMode.collectAsState(DarkModeValues.System)

    val effectiveDarkTheme = when (darkModePreference.value) {
        DarkModeValues.System -> darkTheme
        DarkModeValues.Light -> false
        DarkModeValues.Dark -> true
    }

    val colorScheme = chooseColorScheme(appTheme.value, effectiveDarkTheme)
    val customColors = if (effectiveDarkTheme) darkCustomColors() else lightCustomColors()

    CompositionLocalProvider(
        LocalColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography(),
            shapes = Shapes,
            content = content
        )
    }
}

@Composable
actual fun isAppInDarkTheme(): Boolean {
    val userPreferences: UserPreferences = koinInject()
    val darkModePreference = userPreferences.darkMode.collectAsState(DarkModeValues.System)
    val systemDarkTheme = isSystemInDarkTheme()
    return when (darkModePreference.value) {
        DarkModeValues.System -> systemDarkTheme
        DarkModeValues.Light -> false
        DarkModeValues.Dark -> true
    }
}

@Composable
actual fun resolveDynamicColorScheme(darkTheme: Boolean): ColorScheme? = null

package com.mobileprism.fishing.ui.theme

import android.os.Build
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.mobileprism.fishing.model.datastore.UserPreferences
import androidx.activity.ComponentActivity
import com.mobileprism.fishing.ui.utils.enums.AppThemeValues
import com.mobileprism.fishing.ui.utils.enums.DarkModeValues
import org.koin.compose.koinInject

@Composable
actual fun FishingNotesTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    FishingNotesTheme(
        initialAppTheme = null,
        darkTheme = darkTheme,
        content = content
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FishingNotesTheme(
    initialAppTheme: AppThemeValues? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val activity = LocalActivity.current as? ComponentActivity
    val userPreferences: UserPreferences = koinInject()
    val appTheme = userPreferences.appTheme.collectAsState(initialAppTheme)
    val darkModePreference = userPreferences.darkMode.collectAsState(DarkModeValues.System)

    val effectiveDarkTheme = when (darkModePreference.value) {
        DarkModeValues.System -> darkTheme
        DarkModeValues.Light -> false
        DarkModeValues.Dark -> true
    }

    val colorScheme = chooseColorScheme(appTheme.value, effectiveDarkTheme)

    val customColors = if (effectiveDarkTheme) darkCustomColors() else lightCustomColors()

    DisposableEffect(appTheme.value) {
        activity?.enableEdgeToEdge()
        onDispose { }
    }

    CompositionLocalProvider(
        LocalColors provides customColors
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    }
}

/**
 * Returns the effective dark theme state, respecting the user's dark mode preference.
 */
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
fun chooseColorScheme(appTheme: AppThemeValues?, darkTheme: Boolean): ColorScheme {
    Log.e("AppTheme", "chooseColorScheme: $appTheme")
    return when (appTheme) {
        AppThemeValues.Dynamic -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
            }
        }
        AppThemeValues.Blue -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
        AppThemeValues.Green -> if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
        else -> InitColorScheme
    }
}

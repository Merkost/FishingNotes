package com.mobileprism.fishing.ui.theme

import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.MainActivity
import com.mobileprism.fishing.ui.utils.enums.AppThemeValues
import org.koin.androidx.compose.get

private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200
)

private val LightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = Purple700,
    secondary = Teal200

    /* Other default colors to override
background = Color.White,
surface = Color.White,
onPrimary = Color.White,
onSecondary = Color.Black,
onBackground = Color.Black,
onSurface = Color.Black,
*/
)

private val InitColorPalette = darkColors(
    primary = Color.Transparent,
    primaryVariant = Color.Transparent,
    secondary = Color.Transparent,
    secondaryVariant = Color.Transparent,
)

private val GreenLightColorPalette = lightColors(
    primary = primaryFigmaColor,
    primaryVariant = primaryFigmaDarkColor,
    secondary = secondaryFigmaColor,
    secondaryVariant = secondaryFigmaDarkColor,
)

private val GreenDarkColorPalette = darkColors(
    primary = primaryFigmaDarkColor,
    primaryVariant = primaryFigmaColor,
    secondary = secondaryFigmaDarkColor,
    secondaryVariant = secondaryFigmaColor,
)

private val BlueLightColorPalette = lightColors(
    primary = primaryBlueColor,
    primaryVariant = primaryBlueDarkColor,
    secondary = secondaryBlueColor,
    secondaryVariant = secondaryBlueLightColor,
    onPrimary = primaryWhiteColor,
)

private val BlueDarkColorPalette = darkColors(
    primary = primaryBlueDarkColor,
    primaryVariant = primaryBlueColor,
    secondary = secondaryBlueColor,
    secondaryVariant = secondaryBlueDarkColor,
)

@Composable
fun FishingNotesTheme(
    initialAppTheme: AppThemeValues? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val activity = (LocalContext.current as MainActivity)
    val userPreferences: UserPreferences = get()
    val appTheme = userPreferences.appTheme.collectAsState(initialAppTheme)

    val colors = chooseTheme(appTheme.value, darkTheme)

    val customColors = if (darkTheme) darkCustomColors() else lightCustomColors()

    DisposableEffect(appTheme.value) {
        activity.enableEdgeToEdge()
        onDispose { }
    }

    CompositionLocalProvider(
        LocalColors provides customColors
    ) {
        MaterialTheme(
            colors = colors,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

fun chooseTheme(appTheme: AppThemeValues?, darkTheme: Boolean): Colors {
    Log.e("AppTheme", "chooseTheme: $appTheme")
    return when (appTheme) {
        AppThemeValues.Blue -> if (darkTheme) BlueDarkColorPalette else BlueLightColorPalette
        AppThemeValues.Green -> if (darkTheme) GreenDarkColorPalette else GreenLightColorPalette
        else -> {
            InitColorPalette
        }
    }
}

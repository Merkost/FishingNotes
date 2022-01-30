package com.mobileprism.fishing.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mobileprism.fishing.model.datastore.UserPreferences
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
    val userPreferences: UserPreferences = get()
    val appTheme = userPreferences.appTheme.collectAsState(initialAppTheme)

    val colors = chooseTheme(appTheme.value, darkTheme)

    val customColors = if (darkTheme) darkCustomColors() else lightCustomColors()

    val systemUiController = rememberSystemUiController()
    SideEffect {
        if (darkTheme) {
            systemUiController.apply {
                setSystemBarsColor(color = colors.primary)
                //setStatusBarColor(color = colors.primaryVariant)
            }
        } else {
            systemUiController.apply {
                setSystemBarsColor(color = colors.primary)
                //setStatusBarColor(color = colors.primary)
            }
        }
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
    return when(appTheme) {
        AppThemeValues.Blue -> if (darkTheme) BlueDarkColorPalette else BlueLightColorPalette
        AppThemeValues.Green -> if (darkTheme) GreenDarkColorPalette else GreenLightColorPalette
        else -> { InitColorPalette }
    }
}

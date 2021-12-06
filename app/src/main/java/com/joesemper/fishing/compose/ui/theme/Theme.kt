package com.joesemper.fishing.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.joesemper.fishing.compose.datastore.UserPreferences
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
    initialAppTheme: String? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val userPreferences: UserPreferences = get()
    val appTheme = userPreferences.appTheme.collectAsState(initialAppTheme ?: "null")

    val colors = chooseTheme(appTheme, darkTheme)

    val systemUiController = rememberSystemUiController()
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

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}


fun chooseTheme(appTheme: State<String>, darkTheme: Boolean): Colors {
    return when(appTheme.value) {
        AppThemeValues.Blue.name -> if (darkTheme) BlueDarkColorPalette else BlueLightColorPalette
        AppThemeValues.Green.name -> if (darkTheme) GreenDarkColorPalette else GreenLightColorPalette
        else -> { InitColorPalette }
    }
}

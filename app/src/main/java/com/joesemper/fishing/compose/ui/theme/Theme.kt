package com.joesemper.fishing.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import com.google.accompanist.systemuicontroller.rememberSystemUiController

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
    primaryVariant = primaryBlueLightColor,
    secondary = secondaryBlueColor,
    secondaryVariant = secondaryBlueLightColor,
)

private val BlueDarkColorPalette = darkColors(
    primary = primaryBlueColor,
    primaryVariant = primaryBlueDarkColor,
    secondary = secondaryBlueColor,
    secondaryVariant = secondaryBlueDarkColor,
)

/*val primaryBlueColor = Color(0xFF2196f3)
val primaryBlueColorTransparent = Color(0xF22196F3)
val primaryBlueLightColor = Color(0xFF6ec6ff)
val primaryBlueLightColorTransparent = Color(0x4D6EC6FF)
val primaryBlueDarkColor = Color(0xFF0069c0)
val primaryBlueDarkColorTransparent = Color(0xE60069C0)
val secondaryBlueColor = Color(0xFFff6d00)
val secondaryBlueLightColor = Color(0xFFff9e40)
val secondaryDarkBlueColor = Color(0xFFc43c00)
val primaryBlueTextColor = Color(0xDE000000)
val secondaryBlueTextColor = Color(0x8A000000)
val supportBlueTextColor = Color(0x42000000)*/



@Composable
fun FishingNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    val colors = if (darkTheme) GreenDarkColorPalette else GreenLightColorPalette

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
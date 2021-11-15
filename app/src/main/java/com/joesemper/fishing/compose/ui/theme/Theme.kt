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

     //Other default colors to override
/*background = Color.White,
surface = Color.White,
onPrimary = Color.White,
onSecondary = Color.Black,
onBackground = Color.Black,
onSurface = Color.Black,*/

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

private val TestLightColorPalette = lightColors(
    primary = primaryColor,
    primaryVariant = primaryDarkColor,
    secondary = secondaryFigmaColor,
    secondaryVariant = secondaryFigmaDarkColor,
)

@Composable
fun FishingNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    val colors = if (darkTheme) TestLightColorPalette else TestLightColorPalette

    if (darkTheme) {
        systemUiController.setStatusBarColor(
            color = primaryDarkColor
        )
    } else systemUiController.setStatusBarColor(
        color = primaryDarkColor
    )

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
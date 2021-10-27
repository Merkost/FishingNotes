package com.joesemper.fishing.compose.ui.home

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarData
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * An alternative to [androidx.compose.material.Snackbar] utilizing
 * [com.example.jetsnack.ui.theme.JetsnackColors]
 */
@Composable
fun AppSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    actionOnNewLine: Boolean = false,
    shape: Shape = CircleShape,
    /*backgroundColor: Color = JetsnackTheme.colors.uiBackground,
    contentColor: Color = JetsnackTheme.colors.textSecondary,
    actionColor: Color = JetsnackTheme.colors.brand,*/
    elevation: Dp = 6.dp
) {
    Snackbar(
        snackbarData = snackbarData,
        modifier = modifier,
        actionOnNewLine = actionOnNewLine,
        shape = shape,
        backgroundColor = backgroundColor,
        /*contentColor = contentColor,
        actionColor = actionColor,*/
        elevation = elevation
    )
}
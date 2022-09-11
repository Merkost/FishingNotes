package com.mobileprism.fishing.ui.custom

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CircleButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    enabled: Boolean = true,
    function: @Composable () -> Unit
) {
    OutlinedButton(enabled = enabled, onClick = onClick, modifier = modifier, shape = CircleShape, colors = colors) { function() }
}
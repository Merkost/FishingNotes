package com.mobileprism.fishing.ui.home.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
fun AddItemButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    AppButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        style = AppButtonStyle.Tonal,
        enabled = enabled,
        leadingIcon = rememberVectorPainter(Icons.Default.Add)
    )
}

package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Elevation
import com.mobileprism.fishing.ui.theme.Emphasis

@Composable
fun FloatingControlSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 1f - Emphasis.pressedOverlay),
        shadowElevation = Elevation.card,
        content = content,
    )
}

@Composable
fun FloatingIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 22.dp,
    tint: Color = Color.Unspecified,
    iconModifier: Modifier = Modifier,
) {
    val resolvedTint = if (tint == Color.Unspecified) MaterialTheme.colorScheme.onSurface else tint
    Box(
        modifier = modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                modifier = Modifier.size(iconSize).then(iconModifier),
                imageVector = icon,
                contentDescription = contentDescription,
                tint = resolvedTint,
            )
        }
    }
}

@Composable
fun FloatingIconButton(
    painter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 22.dp,
    tint: Color = Color.Unspecified,
    iconModifier: Modifier = Modifier,
) {
    val resolvedTint = if (tint == Color.Unspecified) MaterialTheme.colorScheme.onSurface else tint
    Box(
        modifier = modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                modifier = Modifier.size(iconSize).then(iconModifier),
                painter = painter,
                contentDescription = contentDescription,
                tint = resolvedTint,
            )
        }
    }
}

package com.mobileprism.fishing.ui.utils

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

fun Modifier.placeholder(
    visible: Boolean,
    color: Color = Color.LightGray,
    shape: Shape = RoundedCornerShape(4.dp),
    highlight: Boolean = true,
): Modifier = if (!visible) this else composed {
    val shimmerColors = listOf(
        color.copy(alpha = 0.9f),
        color.copy(alpha = 0.4f),
        color.copy(alpha = 0.9f),
    )
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    val brush = if (highlight) {
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnim - 500f, 0f),
            end = Offset(translateAnim, 0f),
        )
    } else {
        Brush.linearGradient(colors = listOf(color, color))
    }
    this
        .clip(shape)
        .drawWithContent {
            drawRect(brush = brush)
        }
}

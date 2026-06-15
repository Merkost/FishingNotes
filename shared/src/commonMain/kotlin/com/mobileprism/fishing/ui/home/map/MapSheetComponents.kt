package com.mobileprism.fishing.ui.home.map

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Spacing
import kotlinx.coroutines.launch

private val DismissThreshold = 80.dp

@Composable
fun DragDismissContainer(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        offsetY.snapTo(0f)
    }

    Box(
        modifier = modifier
            .offset { IntOffset(0, offsetY.value.toInt().coerceAtLeast(0)) }
            .pointerInput(Unit) {
                val thresholdPx = DismissThreshold.toPx()
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (offsetY.value > thresholdPx) {
                            onDismiss()
                        } else {
                            coroutineScope.launch {
                                offsetY.animateTo(0f, animationSpec = tween(200))
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            offsetY.animateTo(0f, animationSpec = tween(200))
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetY.snapTo(offsetY.value + dragAmount)
                        }
                    },
                )
            },
    ) {
        content()
    }
}

@Composable
fun DragHandle(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xxs),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 32.dp, height = 4.dp)
                .clip(CircleShape)
                .background(FishingTheme.colorScheme.outlineVariant),
        )
    }
}

@Composable
fun DismissScrim(
    onDismiss: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FishingTheme.colorScheme.scrim.copy(alpha = 0.32f))
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            }
            .clickable(onClick = onDismiss),
    )
}

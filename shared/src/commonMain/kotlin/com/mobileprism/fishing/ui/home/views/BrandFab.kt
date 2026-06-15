package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.mobileprism.fishing.ui.theme.BrandGradients
import com.mobileprism.fishing.ui.theme.Elevation
import com.mobileprism.fishing.ui.theme.FabSize

@Composable
fun BrandFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
        Surface(
            modifier = Modifier.size(FabSize).then(modifier),
            shape = MaterialTheme.shapes.large,
            color = Color.Transparent,
            shadowElevation = Elevation.fab,
        ) {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .background(BrandGradients.primaryDiagonal(MaterialTheme.colorScheme))
                    .indication(interactionSource = interaction, indication = ripple())
                    .combinedClickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onClick,
                        onLongClick = onLongClick,
                    ),
                contentAlignment = Alignment.Center,
                content = content,
            )
        }
    }
}

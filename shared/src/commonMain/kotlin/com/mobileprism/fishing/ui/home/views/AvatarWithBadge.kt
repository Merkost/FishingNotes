package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Elevation

@Composable
fun AvatarWithBadge(
    contentDescription: String,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 96.dp,
    onEdit: (() -> Unit)? = null,
    image: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.size(avatarSize), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape),
            content = image,
        )
        if (onEdit != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = CircleShape,
                    color = FishingTheme.colorScheme.primaryContainer,
                    shadowElevation = Elevation.card,
                ) {
                    AppIconButton(
                        onClick = onEdit,
                        icon = rememberVectorPainter(Icons.Default.Edit),
                        contentDescription = contentDescription,
                    )
                }
            }
        }
    }
}

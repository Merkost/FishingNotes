package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Spacing
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.status_auto
import fishing.shared.generated.resources.status_done
import fishing.shared.generated.resources.status_loading
import fishing.shared.generated.resources.status_offline
import org.jetbrains.compose.resources.stringResource

const val EmptyStatValue: String = "—"

fun shouldShowCountBadge(count: Int): Boolean = count > 0

@Composable
fun IconStatChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    iconRotationDeg: Float = 0f,
) {
    Surface(
        modifier = modifier,
        shape = FishingTheme.shapes.small,
        color = FishingTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .size(16.dp)
                    .let { if (iconRotationDeg != 0f) it.rotate(iconRotationDeg) else it },
                imageVector = icon,
                contentDescription = null,
                tint = FishingTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = label.ifEmpty { EmptyStatValue },
                style = FishingTheme.typography.labelMedium,
                color = FishingTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun IconStatChip(
    iconPainter: androidx.compose.ui.graphics.painter.Painter,
    label: String,
    modifier: Modifier = Modifier,
    iconRotationDeg: Float = 0f,
) {
    Surface(
        modifier = modifier,
        shape = FishingTheme.shapes.small,
        color = FishingTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .size(16.dp)
                    .let { if (iconRotationDeg != 0f) it.rotate(iconRotationDeg) else it },
                painter = iconPainter,
                contentDescription = null,
                tint = FishingTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = label.ifEmpty { EmptyStatValue },
                style = FishingTheme.typography.labelMedium,
                color = FishingTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    if (!shouldShowCountBadge(count)) return
    Box(
        modifier = modifier
            .let { m ->
                if (contentDescription != null) {
                    m.clearAndSetSemantics { this.contentDescription = contentDescription }
                } else m
            }
            .size(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = FishingTheme.shapes.extraLarge,
            color = FishingTheme.colorScheme.error,
            modifier = Modifier.size(20.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (count > 99) "99+" else count.toString(),
                    style = FishingTheme.typography.labelSmall,
                    color = FishingTheme.colorScheme.onError,
                )
            }
        }
    }
}

enum class StatusLabelVariant { Auto, Done, Offline, Loading }

@Composable
fun StatusLabel(
    variant: StatusLabelVariant,
    modifier: Modifier = Modifier,
) {
    val container: Color
    val onContainer: Color
    when (variant) {
        StatusLabelVariant.Auto -> {
            container = FishingTheme.colorScheme.primaryContainer
            onContainer = FishingTheme.colorScheme.onPrimaryContainer
        }
        StatusLabelVariant.Done -> {
            container = FishingTheme.colorScheme.secondaryContainer
            onContainer = FishingTheme.colorScheme.onSecondaryContainer
        }
        StatusLabelVariant.Offline -> {
            container = FishingTheme.colorScheme.errorContainer
            onContainer = FishingTheme.colorScheme.onErrorContainer
        }
        StatusLabelVariant.Loading -> {
            container = FishingTheme.colorScheme.surfaceContainerHigh
            onContainer = FishingTheme.colorScheme.onSurfaceVariant
        }
    }
    val text = when (variant) {
        StatusLabelVariant.Auto -> stringResource(Res.string.status_auto)
        StatusLabelVariant.Done -> stringResource(Res.string.status_done)
        StatusLabelVariant.Offline -> stringResource(Res.string.status_offline)
        StatusLabelVariant.Loading -> stringResource(Res.string.status_loading)
    }
    Surface(modifier = modifier, shape = FishingTheme.shapes.small, color = container) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val leading: ImageVector? = when (variant) {
                StatusLabelVariant.Done -> Icons.Default.Check
                StatusLabelVariant.Offline -> Icons.Default.CloudOff
                else -> null
            }
            if (variant == StatusLabelVariant.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.5.dp,
                    color = onContainer,
                )
            } else if (leading != null) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    imageVector = leading,
                    contentDescription = null,
                    tint = onContainer,
                )
            }
            Text(
                text = text,
                style = FishingTheme.typography.labelSmall,
                color = onContainer,
            )
        }
    }
}

package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Spacing

@Composable
fun MetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    iconTint: Color = FishingTheme.colorScheme.onSurfaceVariant,
    vertical: Boolean = false,
) {
    if (vertical) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
        ) {
            Text(
                text = label,
                style = FishingTheme.typography.labelMedium,
                color = FishingTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                if (icon != null) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = icon,
                        contentDescription = null,
                        tint = iconTint
                    )
                }
                Text(
                    text = value,
                    style = FishingTheme.typography.titleMedium,
                    color = FishingTheme.colorScheme.onSurface
                )
            }
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            if (icon != null) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = icon,
                    contentDescription = null,
                    tint = iconTint
                )
            }
            Column {
                Text(
                    text = label,
                    style = FishingTheme.typography.labelMedium,
                    color = FishingTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = FishingTheme.typography.titleMedium,
                    color = FishingTheme.colorScheme.onSurface
                )
            }
        }
    }
}

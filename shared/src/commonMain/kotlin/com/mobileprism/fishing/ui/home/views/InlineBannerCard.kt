package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Spacing

enum class BannerTone { Info, Warning, Error }

@Composable
fun InlineBannerCard(
    tone: BannerTone,
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    actionLabel: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val container: Color
    val onContainer: Color
    when (tone) {
        BannerTone.Info -> {
            container = FishingTheme.colorScheme.secondaryContainer
            onContainer = FishingTheme.colorScheme.onSecondaryContainer
        }
        BannerTone.Warning -> {
            container = FishingTheme.colorScheme.tertiaryContainer
            onContainer = FishingTheme.colorScheme.onTertiaryContainer
        }
        BannerTone.Error -> {
            container = FishingTheme.colorScheme.errorContainer
            onContainer = FishingTheme.colorScheme.onErrorContainer
        }
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = FishingTheme.shapes.medium,
        color = container,
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = icon,
                contentDescription = null,
                tint = onContainer,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
            ) {
                Text(
                    text = title,
                    style = FishingTheme.typography.titleSmall,
                    color = onContainer,
                )
                if (body != null) {
                    Text(
                        text = body,
                        style = FishingTheme.typography.bodyMedium,
                        color = onContainer,
                    )
                }
                if (actionLabel != null && onClick != null) {
                    TextButton(onClick = onClick) {
                        Text(text = actionLabel, color = onContainer)
                    }
                }
            }
        }
    }
}

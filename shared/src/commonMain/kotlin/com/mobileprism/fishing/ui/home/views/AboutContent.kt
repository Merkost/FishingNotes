package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Spacing
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.app_version
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppHeroHeader(
    title: String,
    logo: Painter,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Icon(
            modifier = Modifier.size(96.dp),
            painter = logo,
            contentDescription = null,
            tint = FishingTheme.colorScheme.primary,
        )
        Text(
            text = title,
            style = FishingTheme.typography.headlineSmall,
            color = FishingTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = FishingTheme.typography.bodyMedium,
                color = FishingTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun VersionLabel(
    version: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = stringResource(Res.string.app_version, version),
        style = FishingTheme.typography.labelMedium,
        color = FishingTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

enum class LabeledIconButtonStyle { Filled, Outlined }

@Composable
fun LabeledIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: LabeledIconButtonStyle = LabeledIconButtonStyle.Outlined,
) {
    val containerColor = when (style) {
        LabeledIconButtonStyle.Filled -> FishingTheme.colorScheme.primary
        LabeledIconButtonStyle.Outlined -> FishingTheme.colorScheme.surface
    }
    val contentColor = when (style) {
        LabeledIconButtonStyle.Filled -> FishingTheme.colorScheme.onPrimary
        LabeledIconButtonStyle.Outlined -> FishingTheme.colorScheme.onSurface
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        shape = FishingTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = Spacing.screenH, vertical = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = icon,
                contentDescription = null,
                tint = when (style) {
                    LabeledIconButtonStyle.Filled -> FishingTheme.colorScheme.onPrimary
                    LabeledIconButtonStyle.Outlined -> FishingTheme.colorScheme.onSurfaceVariant
                },
            )
            Text(
                text = label,
                style = FishingTheme.typography.bodyLarge,
                color = contentColor,
            )
        }
    }
}

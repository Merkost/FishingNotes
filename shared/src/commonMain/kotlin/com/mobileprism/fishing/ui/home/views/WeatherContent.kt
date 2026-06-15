package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Emphasis
import com.mobileprism.fishing.ui.theme.Spacing
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun WeatherMetric(
    label: String,
    icon: DrawableResource,
    value: String,
    modifier: Modifier = Modifier,
    iconTint: Color = LocalContentColor.current,
) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        Text(
            text = label,
            style = FishingTheme.typography.labelSmall,
            color = LocalContentColor.current.copy(alpha = Emphasis.medium),
            textAlign = TextAlign.Center,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(icon),
                contentDescription = null,
                tint = iconTint,
            )
            Text(
                text = value,
                style = FishingTheme.typography.bodyLarge,
                color = LocalContentColor.current,
            )
        }
    }
}

@Composable
fun WeatherStatGrid(
    metrics: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    columns: Int = 2,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        metrics.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                rowItems.forEach { cell ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) { cell() }
                }
                repeat(columns - rowItems.size) {
                    Column(modifier = Modifier.weight(1f)) {}
                }
            }
        }
    }
}

@Composable
fun WeatherDailyForecastRow(
    date: String,
    icon: DrawableResource,
    temperature: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    precipitation: String? = null,
    temperatureLow: String? = null,
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { Text(text = date, style = FishingTheme.typography.bodyLarge) },
        leadingContent = {
            Icon(
                modifier = Modifier.size(28.dp),
                painter = painterResource(icon),
                contentDescription = null,
                tint = FishingTheme.colorScheme.onSurfaceVariant,
            )
        },
        supportingContent = precipitation?.let {
            { Text(text = it, style = FishingTheme.typography.bodyMedium) }
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(
                    text = temperature,
                    style = FishingTheme.typography.titleMedium,
                    color = FishingTheme.colorScheme.onSurface,
                )
                if (temperatureLow != null) {
                    Text(
                        text = temperatureLow,
                        style = FishingTheme.typography.bodyMedium,
                        color = FishingTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
}

@Composable
fun LocationPickerChip(
    label: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    AssistChip(
        modifier = modifier.semantics { role = Role.Button },
        onClick = onClick,
        label = { Text(text = label, maxLines = 1) },
        leadingIcon = leadingIcon?.let {
            { Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(18.dp)) }
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = contentDescription,
                modifier = Modifier.size(AssistChipDefaults.IconSize),
            )
        },
    )
}

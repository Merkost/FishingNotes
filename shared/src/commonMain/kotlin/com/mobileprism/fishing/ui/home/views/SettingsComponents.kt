package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileprism.fishing.ui.theme.Emphasis
import com.mobileprism.fishing.ui.theme.Spacing
import kotlinx.coroutines.delay

@Composable
fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = FishingTheme.typography.titleSmall,
            color = FishingTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = Spacing.xxl, bottom = Spacing.sm)
        )
        Surface(
            shape = FishingTheme.shapes.extraLarge,
            color = FishingTheme.colorScheme.surfaceColorAtElevation(1.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsMenuLink(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = FishingTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(Spacing.md))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = FishingTheme.typography.bodyLarge,
                color = FishingTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = FishingTheme.typography.bodyMedium,
                    color = FishingTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(Spacing.md))
            trailing()
        }
    }
}

@Composable
fun SettingsSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
) {
    SettingsMenuLink(
        title = title,
        subtitle = subtitle,
        icon = icon,
        onClick = { onCheckedChange(!checked) },
        modifier = modifier,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    )
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = Spacing.xxxl + Spacing.sm),
        color = FishingTheme.colorScheme.outlineVariant.copy(alpha = Emphasis.divider)
    )
}

@Composable
fun SettingsHeader(text: String, modifier: Modifier = Modifier) {
    GrayText(text, modifier.padding(14.dp))
}

@Composable
fun GrayText(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Start,
        color = FishingTheme.colorScheme.outline,
        text = text,
        maxLines = 1,
        softWrap = true
    )
}

@Composable
fun SettingsCheckbox(
    title: @Composable () -> Unit,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    subtitle: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        color = FishingTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.size(Spacing.md))
            }
            Column(modifier = Modifier.weight(1f)) {
                title()
                if (subtitle != null) {
                    subtitle()
                }
            }
            androidx.compose.material3.Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

fun contrastingCheckTint(background: Color): Color =
    if (background.luminance() > 0.5f) Color.Black else Color.White

@Composable
fun SelectableColorSwatch(
    color: Color?,
    selected: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val swatchBrush = if (color != null) {
        SolidColor(color)
    } else {
        Brush.sweepGradient(
            listOf(
                Color(0xFFEF5350),
                Color(0xFFAB47BC),
                Color(0xFF42A5F5),
                Color(0xFF66BB6A),
                Color(0xFFFFCA28),
                Color(0xFFEF5350),
            )
        )
    }
    val checkTint = if (color != null) contrastingCheckTint(color) else Color.White

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(swatchBrush)
            .then(
                if (selected) {
                    Modifier.border(2.dp, FishingTheme.colorScheme.onSurface, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(
                onClick = onClick,
                role = Role.RadioButton,
            )
            .semantics {
                this.contentDescription = contentDescription
                this.selected = selected
            },
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = checkTint,
            )
        }
    }
}

@Composable
fun <T> ColorSwatchRow(
    options: List<T>,
    selected: T,
    colorOf: (T) -> Color?,
    contentDescriptionOf: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterHorizontally),
    ) {
        options.forEach { option ->
            SelectableColorSwatch(
                color = colorOf(option),
                selected = option == selected,
                contentDescription = contentDescriptionOf(option),
                onClick = { onSelect(option) },
            )
        }
    }
}

private const val AUTO_DISMISS_DELAY_MS = 200L

@Composable
fun <T> SettingsSelectionDialog(
    title: String,
    options: List<T>,
    currentValue: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    var pendingSelection by remember { mutableStateOf<T?>(null) }

    LaunchedEffect(pendingSelection) {
        val selection = pendingSelection ?: return@LaunchedEffect
        delay(AUTO_DISMISS_DELAY_MS)
        onSelect(selection)
    }

    DefaultDialog(
        primaryText = title,
        onDismiss = onDismiss,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            options.forEach { option ->
                val isSelected = (pendingSelection ?: currentValue) == option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = isSelected,
                            onClick = { pendingSelection = option },
                        )
                        .padding(horizontal = Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { pendingSelection = option },
                    )
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Text(
                        text = label(option),
                        style = FishingTheme.typography.bodyLarge,
                        color = FishingTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandableSettingsSection(
    title: String,
    subtitle: String?,
    icon: ImageVector?,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SettingsMenuLink(
            title = title,
            subtitle = subtitle,
            icon = icon,
            onClick = onToggle,
        )
        AnimatedVisibility(visible = expanded) {
            content()
        }
    }
}

@Composable
fun SettingsNavLink(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
) {
    SettingsMenuLink(
        title = title,
        subtitle = subtitle,
        icon = icon,
        onClick = onClick,
        modifier = modifier,
        trailing = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = FishingTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

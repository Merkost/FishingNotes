package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import com.mobileprism.fishing.ui.theme.Elevation
import com.mobileprism.fishing.ui.theme.IconSize
import com.mobileprism.fishing.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: CornerBasedShape = FishingTheme.shapes.large,
    containerColor: Color = FishingTheme.colorScheme.surface,
    elevation: Dp = Elevation.raisedCard,
    contentPadding: Dp = Spacing.cardPadding,
    content: @Composable ColumnScope.() -> Unit,
) {
    val elevationValues = CardDefaults.cardElevation(defaultElevation = elevation)
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            elevation = elevationValues,
            colors = colors,
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            elevation = elevationValues,
            colors = colors,
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}

@Composable
fun SectionCard(
    icon: Painter,
    title: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppCard(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = FishingTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(IconSize.md),
            )
            AppText(
                text = title,
                style = AppTextStyle.Subtitle,
                color = FishingTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            when {
                trailing != null -> trailing()
                onClick != null -> Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = FishingTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(IconSize.md),
                )
            }
        }
        Column(
            modifier = Modifier.padding(top = Spacing.md),
            content = content,
        )
    }
}

package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Emphasis
import com.mobileprism.fishing.ui.theme.Spacing

@Composable
fun LabeledValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    editContentDescription: String? = null,
) {
    val rowModifier = if (onClick != null) {
        modifier.fillMaxWidth().clickable { onClick() }
    } else {
        modifier.fillMaxWidth()
    }

    Row(
        modifier = rowModifier.padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
        ) {
            Text(
                text = label,
                style = FishingTheme.typography.labelMedium,
                color = FishingTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = FishingTheme.typography.bodyLarge,
                color = FishingTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onClick != null) {
            Icon(
                modifier = Modifier
                    .padding(start = Spacing.sm)
                    .size(20.dp),
                imageVector = Icons.Outlined.Edit,
                contentDescription = editContentDescription,
                tint = FishingTheme.colorScheme.onSurfaceVariant.copy(alpha = Emphasis.hint)
            )
        }
    }
}

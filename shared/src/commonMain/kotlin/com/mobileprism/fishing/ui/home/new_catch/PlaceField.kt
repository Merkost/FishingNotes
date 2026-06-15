package com.mobileprism.fishing.ui.home.new_catch

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Spacing

@Composable
fun PlaceField(
    modifier: Modifier = Modifier,
    placeTitle: String?,
    placeColor: Int?,
    placeholder: String,
    trailing: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Surface(
        color = FishingTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            placeColor?.let { color ->
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(color),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(Spacing.sm))
            }
            Text(
                text = placeTitle ?: placeholder,
                style = FishingTheme.typography.bodyLarge,
                color = if (placeTitle != null) FishingTheme.colorScheme.onSurface
                else FishingTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            trailing()
        }
    }
}

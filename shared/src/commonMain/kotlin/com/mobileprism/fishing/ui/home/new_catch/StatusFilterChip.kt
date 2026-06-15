package com.mobileprism.fishing.ui.home.new_catch

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Spacing

sealed interface ChipBadge {
    data object None : ChipBadge
    data object Done : ChipBadge
    data class Count(val value: Int) : ChipBadge
    data object Loading : ChipBadge
}

@Composable
fun StatusFilterChip(
    label: String,
    selected: Boolean,
    badge: ChipBadge = ChipBadge.None,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label)
                when (badge) {
                    ChipBadge.None -> Unit
                    ChipBadge.Done -> {
                        Spacer(Modifier.width(Spacing.xs))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = FishingTheme.colorScheme.primary
                        )
                    }
                    is ChipBadge.Count -> {
                        if (badge.value > 0) {
                            Spacer(Modifier.width(Spacing.xs))
                            Text(
                                text = badge.value.toString(),
                                style = FishingTheme.typography.labelSmall,
                                color = FishingTheme.colorScheme.primary
                            )
                        }
                    }
                    ChipBadge.Loading -> {
                        Spacer(Modifier.width(Spacing.xs))
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }
    )
}

package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import com.mobileprism.fishing.ui.theme.Elevation
import com.mobileprism.fishing.ui.theme.Motion

data class AppNavItem(
    val key: String,
    val icon: ImageVector,
    val label: String,
)

@Composable
fun appBottomNavItemColors(): NavigationBarItemColors = NavigationBarItemDefaults.colors(
    selectedIconColor = FishingTheme.colorScheme.onPrimaryContainer,
    selectedTextColor = FishingTheme.colorScheme.primary,
    indicatorColor = FishingTheme.colorScheme.primaryContainer,
    unselectedIconColor = FishingTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = FishingTheme.colorScheme.onSurfaceVariant,
)

@Composable
fun AppBottomNavigation(
    items: List<AppNavItem>,
    currentKey: String,
    onSelect: (AppNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = FishingTheme.colorScheme.surface,
        contentColor = FishingTheme.colorScheme.onSurface,
        tonalElevation = Elevation.level0,
    ) {
        items.forEach { item ->
            val selected = item.key == currentKey
            val iconScale by animateFloatAsState(
                targetValue = if (selected) 1f else 0.92f,
                animationSpec = Motion.navIndicatorFloat(),
                label = "navIconScale",
            )
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(item) },
                colors = appBottomNavItemColors(),
                icon = {
                    Icon(
                        modifier = Modifier.scale(iconScale),
                        imageVector = item.icon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = FishingTheme.typography.labelMedium,
                        maxLines = 1,
                    )
                },
                alwaysShowLabel = true,
            )
        }
    }
}

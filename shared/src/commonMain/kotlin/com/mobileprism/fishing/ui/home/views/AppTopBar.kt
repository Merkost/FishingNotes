package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.back
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun appTopBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = FishingTheme.colorScheme.surface,
    scrolledContainerColor = FishingTheme.colorScheme.surfaceContainer,
    titleContentColor = FishingTheme.colorScheme.onSurface,
    navigationIconContentColor = FishingTheme.colorScheme.onSurfaceVariant,
    actionIconContentColor = FishingTheme.colorScheme.onSurfaceVariant,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = { AppTopBarTitle(title = title, subtitle = subtitle) },
        navigationIcon = {
            if (navigationIcon != null && onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = stringResource(Res.string.back),
                        tint = FishingTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        actions = actions,
        colors = appTopBarColors(),
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLargeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: ImageVector? = Icons.AutoMirrored.Filled.ArrowBack,
    onNavigationClick: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    LargeTopAppBar(
        modifier = modifier,
        title = { AppTopBarTitle(title = title, subtitle = subtitle) },
        navigationIcon = {
            if (navigationIcon != null && onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = stringResource(Res.string.back),
                        tint = FishingTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = FishingTheme.colorScheme.surface,
            scrolledContainerColor = FishingTheme.colorScheme.surfaceContainer,
            titleContentColor = FishingTheme.colorScheme.onSurface,
            navigationIconContentColor = FishingTheme.colorScheme.onSurfaceVariant,
            actionIconContentColor = FishingTheme.colorScheme.onSurfaceVariant,
        ),
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun AppTopBarTitle(title: String, subtitle: String?) {
    Column {
        AppText(text = title, style = AppTextStyle.Title)
        if (subtitle != null) {
            AppText(
                text = subtitle,
                style = AppTextStyle.Caption,
                color = FishingTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

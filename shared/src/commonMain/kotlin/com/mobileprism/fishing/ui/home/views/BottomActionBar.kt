package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mobileprism.fishing.ui.theme.Elevation
import com.mobileprism.fishing.ui.theme.Spacing

@Composable
fun BottomActionBar(
    primaryText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = FishingTheme.colorScheme.surface,
            tonalElevation = Elevation.level2,
            shadowElevation = Elevation.level2,
        ) {
            AppButton(
                text = primaryText,
                onClick = onClick,
                style = AppButtonStyle.Filled,
                enabled = enabled,
                loading = loading,
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screenH, vertical = Spacing.md),
            )
        }
    }
}

package com.mobileprism.fishing.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
expect fun FishingNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
)

@Composable
expect fun isAppInDarkTheme(): Boolean

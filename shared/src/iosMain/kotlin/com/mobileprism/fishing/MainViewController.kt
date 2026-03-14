package com.mobileprism.fishing

import androidx.compose.ui.window.ComposeUIViewController
import com.mobileprism.fishing.ui.FishingNotesApp
import com.mobileprism.fishing.ui.theme.FishingNotesTheme

fun MainViewController() = ComposeUIViewController {
    FishingNotesTheme {
        FishingNotesApp()
    }
}

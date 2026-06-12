package com.mobileprism.fishing

import androidx.compose.ui.window.ComposeUIViewController
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import com.mobileprism.fishing.di.initKoinIos
import com.mobileprism.fishing.ui.FishingNotesApp
import com.mobileprism.fishing.ui.theme.FishingNotesTheme
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    GoogleAuthProvider.create(GoogleAuthCredentials(serverId = BuildKonfig.GOOGLE_WEB_CLIENT_ID))
    initKoinIos()
    return ComposeUIViewController {
        FishingNotesTheme {
            FishingNotesApp()
        }
    }
}

fun mapsApiKey(): String = BuildKonfig.MAPS_API_KEY

package com.mobileprism.fishing.ui

import androidx.compose.runtime.staticCompositionLocalOf

fun interface GoogleLoginHandler {
    suspend fun startGoogleLogin()
}

val LocalGoogleLoginHandler = staticCompositionLocalOf<GoogleLoginHandler> {
    error("No GoogleLoginHandler provided")
}

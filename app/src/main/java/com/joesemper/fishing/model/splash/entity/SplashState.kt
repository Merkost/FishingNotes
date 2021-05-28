package com.joesemper.fishing.model.splash.entity

sealed class SplashState {
    class Error(val error: Throwable) : SplashState()
    object Authorised : SplashState()
    object NotAuthorised : SplashState()
}
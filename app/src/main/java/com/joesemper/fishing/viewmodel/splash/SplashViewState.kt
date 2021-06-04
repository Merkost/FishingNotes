package com.joesemper.fishing.viewmodel.splash

sealed class SplashViewState {
    class Error(val error: Throwable) : SplashViewState()
    object Authorised : SplashViewState()
    object NotAuthorised : SplashViewState()
}
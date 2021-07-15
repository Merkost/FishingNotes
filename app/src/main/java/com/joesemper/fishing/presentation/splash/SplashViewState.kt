package com.joesemper.fishing.presentation.splash

import com.joesemper.fishing.model.common.User

sealed class SplashViewState {
    object Loading : SplashViewState()
    class Success(val user: User?): SplashViewState()
    class Error(val error: Throwable) : SplashViewState()
}
package com.joesemper.fishing.viewmodel.splash

import com.joesemper.fishing.model.entity.user.User

sealed class SplashViewState {
    object Loading : SplashViewState()
    class Success(val user: User?): SplashViewState()
    class Error(val error: Throwable) : SplashViewState()
}
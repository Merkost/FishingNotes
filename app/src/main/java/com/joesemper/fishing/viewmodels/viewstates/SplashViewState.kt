package com.joesemper.fishing.viewmodels.viewstates

import com.joesemper.fishing.data.entity.common.User

sealed class SplashViewState {
    object Loading : SplashViewState()
    class Success(val user: User?): SplashViewState()
    class Error(val error: Throwable) : SplashViewState()
}
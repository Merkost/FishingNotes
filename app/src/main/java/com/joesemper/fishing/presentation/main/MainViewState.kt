package com.joesemper.fishing.presentation.main

import com.joesemper.fishing.model.common.User

sealed class MainViewState {
    object Loading: MainViewState()
    class Success(val user: User?): MainViewState()
    class Error(val error: Throwable): MainViewState()
}

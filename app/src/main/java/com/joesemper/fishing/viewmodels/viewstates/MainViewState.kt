package com.joesemper.fishing.viewmodels.viewstates

import com.joesemper.fishing.data.entity.common.User

sealed class MainViewState {
    object Loading: MainViewState()
    class Success(val user: User?): MainViewState()
    class Error(val error: Throwable): MainViewState()
}

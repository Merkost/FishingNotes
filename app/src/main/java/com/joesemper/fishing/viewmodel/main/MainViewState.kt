package com.joesemper.fishing.viewmodel.main

import com.joesemper.fishing.model.entity.user.User
import com.joesemper.fishing.viewmodel.base.ViewState

sealed class MainViewState: ViewState {
    class LoggedIn(val user: User): MainViewState()
    object LoggedOut: MainViewState()
    class Error(val error: Throwable): MainViewState()
}

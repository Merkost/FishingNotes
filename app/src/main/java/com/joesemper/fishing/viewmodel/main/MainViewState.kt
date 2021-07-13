package com.joesemper.fishing.viewmodel.main

import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.viewmodel.base.ViewState

sealed class MainViewState: ViewState {
    object Loading: MainViewState()
    class Success(val user: User?): MainViewState()
    class Error(val error: Throwable): MainViewState()
}

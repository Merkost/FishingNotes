package com.joesemper.fishing.viewmodels.viewstates

import com.joesemper.fishing.data.entity.common.User

sealed class LoginViewState {
    object Loading : LoginViewState()
    class Success(val user: User?): LoginViewState()
    class Error(val error: Throwable) : LoginViewState()
}
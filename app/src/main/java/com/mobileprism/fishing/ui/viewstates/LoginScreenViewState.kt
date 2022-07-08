package com.mobileprism.fishing.ui.viewstates

sealed class LoginScreenViewState {
    object LoginSuccess : LoginScreenViewState()
    object NotLoggedIn : LoginScreenViewState()
    object Loading : LoginScreenViewState()
    class Error(val error: Throwable) : LoginScreenViewState()
}
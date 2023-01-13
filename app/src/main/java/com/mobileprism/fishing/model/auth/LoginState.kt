package com.mobileprism.fishing.model.auth

import com.mobileprism.fishing.model.entity.FishingResponse
import com.mobileprism.fishing.model.entity.user.Token
import com.mobileprism.fishing.model.entity.user.UserData

sealed class LoginState {
    object LoggedIn : LoginState()
    class LoginFailure(val fishingResponse: FishingResponse) : LoginState()
    object NotLoggedIn : LoginState()
}

sealed interface AuthState {
    class LoggedIn(user: UserData, token: Token) : AuthState
    object NotLoggedIn : AuthState
}
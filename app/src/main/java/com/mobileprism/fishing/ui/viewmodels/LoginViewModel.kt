package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.common.EmailPassword
import com.mobileprism.fishing.domain.use_cases.users.*
import com.mobileprism.fishing.model.auth.LoginState
import com.mobileprism.fishing.ui.viewstates.LoginScreenViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginViewModel(
    private val subscribeOnLoginStatus: SubscribeOnLoginState,
    private val registerNewUserUseCase: RegisterNewUserUseCase,
    private val signInUserUseCase: SignInUserUserCase,
    private val signInUserWithGoogleUseCase: SignInUserWithGoogleUseCase,
    private val skipAuthorizationUseCase: SkipAuthorizationUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<LoginScreenViewState> =
        MutableStateFlow(LoginScreenViewState.NotLoggedIn)
    val uiState = _uiState.asStateFlow()

    init {
        subscribeOnLoginState()
    }

    private fun subscribeOnLoginState() {
        viewModelScope.launch {
            subscribeOnLoginStatus().collectLatest {
                when (it) {
                    is LoginState.LoggedIn -> {
                        _uiState.value = LoginScreenViewState.LoginSuccess
                    }
                    LoginState.GoogleAuthRequest -> {}
                    is LoginState.LoginFailure -> {
                        _uiState.value = (LoginScreenViewState.NotLoggedIn)
                        handleError(it.throwable)
                    }
                    LoginState.NotLoggedIn -> {
                        _uiState.value = (LoginScreenViewState.NotLoggedIn)
                    }
                }
            }
        }
    }

    fun registerNewUser(emailPassword: EmailPassword) {
        _uiState.value = LoginScreenViewState.Loading

        viewModelScope.launch {
            registerNewUserUseCase(emailPassword)
        }
    }

    fun signInUser(emailPassword: EmailPassword) {
        _uiState.value = LoginScreenViewState.Loading

        viewModelScope.launch {
            signInUserUseCase(emailPassword)
        }
    }

    fun skipAuthorization() {
        _uiState.value = LoginScreenViewState.Loading

        viewModelScope.launch {
            skipAuthorizationUseCase()
        }
    }

    private fun handleError(error: Throwable) {
        _uiState.value = LoginScreenViewState.Error(error)
    }

    fun continueWithGoogle() {
        _uiState.value = LoginScreenViewState.Loading

        viewModelScope.launch {
            signInUserWithGoogleUseCase()
        }
    }

}



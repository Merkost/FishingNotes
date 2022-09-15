package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.common.EmailPassword
import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.domain.use_cases.users.RegisterNewUserUseCase
import com.mobileprism.fishing.domain.use_cases.users.SignInUserUserCase
import com.mobileprism.fishing.domain.use_cases.users.SkipAuthorizationUseCase
import com.mobileprism.fishing.model.auth.LoginState
import com.mobileprism.fishing.ui.viewstates.LoginScreenViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// TODO: Delete many UseCases
class LoginViewModel(
    private val authManager: AuthManager,
    private val registerNewUserUseCase: RegisterNewUserUseCase,
    private val signInUserUseCase: SignInUserUserCase,
    private val skipAuthorizationUseCase: SkipAuthorizationUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<LoginScreenViewState> =
        MutableStateFlow(LoginScreenViewState.NotLoggedIn)
    val uiState = _uiState.asStateFlow()

    init {
        subscribeOnLoginState()
    }

    fun googleAuthError(exception: Exception?) {
        _uiState.update { LoginScreenViewState.Error(exception ?: Throwable()) }
    }

    private fun subscribeOnLoginState() {
        viewModelScope.launch {
            authManager.loginState.collectLatest {
                when (it) {
                    // TODO: Deal with LoginScreenViewState mess
                    is LoginState.LoggedIn -> {
                        _uiState.value = LoginScreenViewState.LoginSuccess
                    }
                    LoginState.GoogleAuthInProcess -> {
                        _uiState.update { LoginScreenViewState.Loading }
                    }
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
        _uiState.update { LoginScreenViewState.Loading }

        viewModelScope.launch {
            registerNewUserUseCase(emailPassword)
        }
    }

    fun signInUser(emailPassword: EmailPassword) {
        _uiState.update { LoginScreenViewState.Loading }

        viewModelScope.launch {
            signInUserUseCase(emailPassword)
        }
    }

    fun skipAuthorization() {
        _uiState.update { LoginScreenViewState.Loading }

        viewModelScope.launch {
            skipAuthorizationUseCase()
        }
    }

    private fun handleError(error: Throwable) {
        _uiState.update { LoginScreenViewState.Error(error) }
    }

    fun continueWithGoogle() {
        viewModelScope.launch {
            _uiState.update { LoginScreenViewState.Loading }
            authManager.googleLogin()
        }
    }

}



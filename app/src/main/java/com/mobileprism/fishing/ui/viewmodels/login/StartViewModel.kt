package com.mobileprism.fishing.ui.viewmodels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.auth.EmailPassword
import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.model.auth.LoginState
import com.mobileprism.fishing.ui.viewstates.LoginScreenViewState
import com.mobileprism.fishing.utils.LoginInputType
import com.mobileprism.fishing.utils.checkLoginInputType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StartViewModel(
    private val authManager: AuthManager,
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
            authManager.registerNewUserWithEmail(emailPassword)
        }
    }

    fun signInUser(loginOrEmail: String, password: String) {
        _uiState.update { LoginScreenViewState.Loading }

        viewModelScope.launch {

            when (checkLoginInputType(loginOrEmail)) {
                LoginInputType.Email -> {
                    authManager.loginUser(EmailPassword(loginOrEmail, password))
                }
                LoginInputType.Login -> TODO()
                LoginInputType.IncorrectFormat -> TODO()
            }
        }
    }

    fun skipAuthorization() {
        _uiState.update { LoginScreenViewState.Loading }

        viewModelScope.launch {
            authManager.skipAuthorization()
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



package com.mobileprism.fishing.ui.viewmodels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.BuildConfig
import com.mobileprism.fishing.domain.entity.auth.EmailPassword
import com.mobileprism.fishing.domain.entity.auth.UsernamePassword
import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.model.auth.LoginState
import com.mobileprism.fishing.utils.LoginInputType
import com.mobileprism.fishing.utils.checkLoginInputType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginScreenViewState())
    val uiState = _uiState.asStateFlow()

    private val _loginInfo = MutableStateFlow(LoginInfo())
    val loginInfo = _loginInfo.asStateFlow()

    private var loginJob: Job? = null

    init {
        subscribeOnLoginState()
    }

    fun cancelLogin() {
        loginJob?.cancel()
        _uiState.update { _uiState.value.copy(isLoading = false) }
    }

    fun setLogin(login: String) {
        _loginInfo.update { _loginInfo.value.copy(login = login) }
        _uiState.update { _uiState.value.copy(isLoginError = false) }
    }

    fun setPassword(password: String) {
        _loginInfo.update { _loginInfo.value.copy(password = password) }
        _uiState.update { _uiState.value.copy(isPasswordError = false) }
    }


    fun validateLogin(skipEmpty: Boolean = false) {
        if (skipEmpty && _loginInfo.value.login.isEmpty()) return
        when (checkLoginInputType(_loginInfo.value.login)) {
            is LoginInputType.IncorrectFormat -> {
                _uiState.update { _uiState.value.copy(isLoginError = true) }
            }
            else -> {
                _uiState.update { _uiState.value.copy(isLoginError = false) }
            }
        }
    }

    fun validatePassword() {
        when {
            // TODO:  
            /*_loginInfo.value.password.toList().size < 8 -> {
                _loginInfo.update { _loginInfo.value.copy(isPasswordError = true) }
            }*/
            else -> {
                _uiState.update { _uiState.value.copy(isPasswordError = false) }
            }
        }
    }


    fun signInUser() {
        _uiState.update {
            _uiState.value.copy(
                isLoading = true,
                isError = false
            )
        }

        loginJob = viewModelScope.launch {
            if (BuildConfig.DEBUG) delay(2000)
            _loginInfo.value.let { loginInfo ->

                when (checkLoginInputType(loginInfo.login)) {
                    LoginInputType.Email -> {
                        authManager.loginUser(EmailPassword(loginInfo.login, loginInfo.password))
                    }
                    LoginInputType.Login -> {
                        authManager.loginUser(UsernamePassword(loginInfo.login, loginInfo.password))
                    }
                    LoginInputType.IncorrectFormat -> {
                        _uiState.update {
                            _uiState.value.copy(
                                isLoading = false,
                                isLoginError = true
                            )
                        }
                    }
                }
            }
        }
    }

    private fun subscribeOnLoginState() {
        viewModelScope.launch {
            authManager.loginState.collectLatest { loginState ->
                when (loginState) {
                    is LoginState.LoggedIn -> {
                        _uiState.update {
                            _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                isError = false
                            )
                        }
                    }
                    is LoginState.LoginFailure -> {
                        _uiState.update {
                            _uiState.value.copy(
                                isLoading = false,
                                isError = true,
                                errorText = loginState.throwable.localizedMessage
                            )
                        }
                    }
                    LoginState.NotLoggedIn -> {
                        _uiState.update {
                            _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = false,
                                isError = false
                            )
                        }
                    }
                }
            }
        }
    }
}

data class LoginInfo(
    val login: String = "",
    val password: String = "",
)

data class LoginScreenViewState(
    val isLoading: Boolean = false,
    val isLoginError: Boolean = false,
    val isPasswordError: Boolean = false,
    val isError: Boolean = false,
    val errorText: String? = "",
    val isLoggedIn: Boolean = false
)
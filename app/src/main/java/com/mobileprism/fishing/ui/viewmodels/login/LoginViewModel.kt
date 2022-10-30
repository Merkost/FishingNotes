package com.mobileprism.fishing.ui.viewmodels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.BuildConfig
import com.mobileprism.fishing.domain.entity.auth.EmailPassword
import com.mobileprism.fishing.domain.entity.auth.UsernamePassword
import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.domain.use_cases.validation.ValidationResult
import com.mobileprism.fishing.domain.use_cases.validation.ValidationUseCase
import com.mobileprism.fishing.model.auth.LoginState
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.utils.LoginInputType
import com.mobileprism.fishing.utils.checkLoginInputType
import com.mobileprism.fishing.utils.network.ConnectionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authManager: AuthManager,
    private val connectionManager: ConnectionManager,
    private val validationUseCase: ValidationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState?>(null)
    val uiState = _uiState.asStateFlow()

    private val _authInfo = MutableStateFlow(AuthInfo())
    val loginInfo = _authInfo.asStateFlow()

    private var loginJob: Job? = null

    init {
        subscribeOnAuthState()
    }

    fun cancelLogin() {
        loginJob?.cancel()
        _uiState.update { null }
    }

    fun setLogin(login: String) {
        _authInfo.update {
            _authInfo.value.copy(
                login = login,
                loginError = ValidationResult(true)
            )
        }
    }

    fun setPassword(password: String) {
        _authInfo.update {
            _authInfo.value.copy(
                password = password,
                passwordError = ValidationResult(true)
            )
        }
    }

    fun validateLogin(skipEmpty: Boolean = false) {
        if (skipEmpty && _authInfo.value.login.isEmpty()) return

        loginInfo.value.apply {
            _authInfo.update {
                it.copy(loginError = validationUseCase.validateLogin(login))
            }
        }
    }

    fun signInUser() {
        loginJob = viewModelScope.launch {
            loginInfo.value.apply {

                val loginResult = validationUseCase.validateLogin(login)
                val passwordResult = validationUseCase.validatePassword(password)

                val hasError = listOf(
                    loginResult,
                    passwordResult,
                ).any { !it.successful }


                _authInfo.update {
                    it.copy(
                        loginError = loginResult,
                        passwordError = passwordResult,
                    )
                }

                if (hasError) return@launch

                _uiState.update { UiState.InProgress }
                if (BuildConfig.DEBUG) delay(2000)
                _authInfo.value.let { loginInfo ->

                    when (checkLoginInputType(loginInfo.login)) {
                        LoginInputType.Email -> {
                            authManager.loginUser(
                                EmailPassword(
                                    loginInfo.login,
                                    loginInfo.password
                                )
                            )
                        }
                        LoginInputType.Username -> {
                            authManager.loginUser(
                                UsernamePassword(
                                    loginInfo.login,
                                    loginInfo.password
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun subscribeOnAuthState() {
        viewModelScope.launch {
            authManager.loginState.collectLatest { loginState ->
                when (loginState) {
                    is LoginState.LoginFailure -> {
                        _uiState.update { UiState.Error }

                        /*_uiState.update {
                            _uiState.value.copy(
                                isLoading = false,
                                isError = true,
                                errorText = loginState.throwable.localizedMessage
                            )
                        }*/
                    }
                    is LoginState.LoggedIn -> {
                        _uiState.update { UiState.Success }
                        /*_uiState.update {
                            _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                isError = false
                            )
                        }*/
                    }
                    LoginState.NotLoggedIn -> {
                        // TODO:
                        /*_uiState.update {
                            _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = false,
                                isError = false
                            )
                        }*/
                    }
                }
            }
        }
    }
}

data class AuthInfo(
    val login: String = "",
    val loginError: ValidationResult = ValidationResult(true),
    val password: String = "",
    val passwordError: ValidationResult = ValidationResult(true),
)

data class AuthUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isLoggedIn: Boolean = false
)
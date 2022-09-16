package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.common.EmailPassword
import com.mobileprism.fishing.domain.entity.common.UsernamePassword
import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.model.auth.LoginState
import com.mobileprism.fishing.utils.LoginInputType
import com.mobileprism.fishing.utils.checkLoginInputType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginScreenViewModel(
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState: MutableStateFlow<LoginScreenViewState> =
        MutableStateFlow(LoginScreenViewState())
    val uiState = _uiState.asStateFlow()

    init {
        subscribeOnLoginState()
    }


    fun signInUser(usernameOrEmail: String, password: String) {
        _uiState.update {
            _uiState.value.copy(
                isLoading = true,
                isLoginInputError = false,
                isPasswordError = false,
                isError = false
            )
        }

        viewModelScope.launch {

            when (checkLoginInputType(usernameOrEmail)) {
                LoginInputType.Email -> {
                    authManager.loginUser(EmailPassword(usernameOrEmail, password))
                }
                LoginInputType.Login -> {
                    authManager.registerNewUserWithUserName(
                        UsernamePassword(
                            usernameOrEmail,
                            password
                        )
                    )
                }
                LoginInputType.IncorrectFormat -> {
                    _uiState.update {
                        _uiState.value.copy(
                            isLoading = false,
                            isLoginInputError = true
                        )
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

data class LoginScreenViewState(
    val isLoading: Boolean = false,
    val isLoginInputError: Boolean = false,
    val isPasswordError: Boolean = false,
    val isError: Boolean = false,
    val errorText: String? = "",
    val isLoggedIn: Boolean = false
)
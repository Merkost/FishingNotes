package com.mobileprism.fishing.ui.viewmodels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.auth.EmailPassword
import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.domain.use_cases.validation.ValidationResult
import com.mobileprism.fishing.domain.use_cases.validation.ValidationUseCase
import com.mobileprism.fishing.model.auth.LoginState
import com.mobileprism.fishing.ui.viewstates.FishingViewState
import com.mobileprism.fishing.utils.network.ConnectionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authManager: AuthManager,
    private val connectionManager: ConnectionManager,
    private val validationUseCase: ValidationUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<FishingViewState<Unit>?> = MutableStateFlow(null)
    val uiState = _uiState.asStateFlow()

    private val _registerInfo = MutableStateFlow(RegisterInfo())
    val registerInfo = _registerInfo.asStateFlow()

    private var registerJob: Job? = null

    init {
        subscribeOnAuthState()
    }

    fun cancelRegister() {
        registerJob?.cancel()
        _uiState.update { null }
    }

    fun onEmailSet(newEmail: String) {
        _registerInfo.update { it.copy(email = newEmail) }
    }

    fun onPasswordSet(password: String) {
        _registerInfo.update { it.copy(password = password) }
    }

    fun onRepeatPasswordSet(repeatPassword: String) {
        _registerInfo.update { it.copy(repeatPassword = repeatPassword) }
        registerInfo.value.let { regInfo ->
            if (regInfo.password.startsWith(repeatPassword, false).not()
                || regInfo.password.length < repeatPassword.length
            ) {
                _registerInfo.update {
                    it.copy(
                        repeatPasswordError =
                        validationUseCase.validateRepeatedPassword(it.password, repeatPassword)
                    )
                }
            } else {
                _registerInfo.update { it.copy(repeatPasswordError = ValidationResult(true)) }
            }
        }
    }

    fun onTermsSet(newValue: Boolean) {
        _registerInfo.update {
            it.copy(terms = newValue, termsError = ValidationResult(true))
        }
    }


    fun registerNewUser() {
        registerJob = viewModelScope.launch {
            registerInfo.value.apply {

                val emailResult = validationUseCase.validateEmail(email)
                val passwordResult = validationUseCase.validatePassword(password)
                val repeatPasswordResult =
                    validationUseCase.validateRepeatedPassword(password, repeatPassword)
                val termsResult = validationUseCase.validateTerms(terms)

                val hasError = listOf(
                    emailResult,
                    passwordResult,
                    repeatPasswordResult,
                    termsResult
                ).any { !it.successful }


                _registerInfo.update {
                    it.copy(
                        emailError = emailResult,
                        passwordError = passwordResult,
                        repeatPasswordError = repeatPasswordResult,
                        termsError = termsResult
                    )
                }

                if (hasError) return@launch

                _uiState.update { FishingViewState.Loading }
                authManager.registerNewUser(EmailPassword(email = email, password = password))
            }
        }
    }


    fun validateEmailInput() {
        _registerInfo.update {
            it.copy(emailError = validationUseCase.validateEmail(_registerInfo.value.email))
        }
    }

    fun validatePasswordInput() {
        _registerInfo.update {
            it.copy(passwordError = validationUseCase.validatePassword(it.password))
        }
    }

    private fun subscribeOnAuthState() {
        viewModelScope.launch {
            authManager.loginState.collectLatest { loginState ->
                when (loginState) {
                    is LoginState.LoginFailure -> {
                        _uiState.update { FishingViewState.Error(loginState.fishingResponse) }
                    }
                    is LoginState.LoggedIn -> {
                        _uiState.update { FishingViewState.Success(Unit) }
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

data class RegisterInfo(
    val email: String = "",
    val emailError: ValidationResult = ValidationResult(true),
    val password: String = "",
    val passwordError: ValidationResult = ValidationResult(true),
    val repeatPassword: String = "",
    val repeatPasswordError: ValidationResult = ValidationResult(true),
    val terms: Boolean = false,
    val termsError: ValidationResult = ValidationResult(true),
)



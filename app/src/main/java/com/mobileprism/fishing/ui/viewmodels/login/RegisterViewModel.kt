package com.mobileprism.fishing.ui.viewmodels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.auth.EmailPassword
import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.domain.use_cases.validation.ValidationResult
import com.mobileprism.fishing.domain.use_cases.validation.ValidationUseCase
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.utils.network.ConnectionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authManager: AuthManager,
    private val connectionManager: ConnectionManager,
    private val validationUseCase: ValidationUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<UiState?> = MutableStateFlow(null)
    val uiState = _uiState.asStateFlow()

    private val _registerInfo = MutableStateFlow(RegisterInfo())
    val registerInfo = _registerInfo.asStateFlow()


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
        viewModelScope.launch {
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

                _uiState.update { UiState.InProgress }
                authManager.registerNewUser(EmailPassword(email = email, password = password))
            }
        }
    }


    private fun handleError(error: Throwable) {
        //_uiState.update { LoginScreenViewState.Error(error) }
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



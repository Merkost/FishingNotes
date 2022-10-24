package com.mobileprism.fishing.ui.viewmodels.login

import androidx.lifecycle.ViewModel
import com.mobileprism.fishing.domain.use_cases.validation.ValidationResult
import com.mobileprism.fishing.domain.use_cases.validation.ValidationUseCase
import com.mobileprism.fishing.ui.home.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ForgotPasswordViewModel(
    private val validationUseCase: ValidationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState?>(null)
    val uiState = _uiState.asStateFlow()

    private val _loginInfo = MutableStateFlow(LoginInfo())
    val loginInfo = _loginInfo.asStateFlow()


    fun setLogin(login: String) {
        _loginInfo.update {
            _loginInfo.value.copy(
                login = login,
                loginError = ValidationResult(true)
            )
        }
    }

    fun validateLogin(skipEmpty: Boolean = false) {
        if (skipEmpty && _loginInfo.value.login.isEmpty()) return

        loginInfo.value.apply {
            _loginInfo.update {
                it.copy(loginError = validationUseCase.validateLogin(login))
            }
        }
    }

}
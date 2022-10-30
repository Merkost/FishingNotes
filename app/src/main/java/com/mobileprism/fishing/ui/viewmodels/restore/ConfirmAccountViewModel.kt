package com.mobileprism.fishing.ui.viewmodels.restore

import androidx.lifecycle.ViewModel
import com.mobileprism.fishing.domain.repository.RestoreRepository
import com.mobileprism.fishing.domain.use_cases.validation.ValidationResult
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.viewmodels.login.AuthInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ConfirmAccountViewModel(
    private val restoreRepository: RestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState?>(null)
    val uiState = _uiState.asStateFlow()

    private val _authInfo = MutableStateFlow(AuthInfo())
    val loginInfo = _authInfo.asStateFlow()


    fun setLogin(login: String) {
        _authInfo.update {
            _authInfo.value.copy(
                login = login,
                loginError = ValidationResult(true)
            )
        }
    }

    fun validateLogin(skipEmpty: Boolean = false) {
        if (skipEmpty && _authInfo.value.login.isEmpty()) return

        loginInfo.value.apply {
            /*_loginInfo.update {
                it.copy(loginError = validationUseCase.validateLogin(login))
            }*/
        }
    }

}
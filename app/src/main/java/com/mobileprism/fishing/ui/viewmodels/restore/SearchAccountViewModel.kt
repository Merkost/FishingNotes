package com.mobileprism.fishing.ui.viewmodels.restore

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.auth.restore.RestoreRemoteConfirm
import com.mobileprism.fishing.domain.entity.auth.restore.RestoreRemoteFind
import com.mobileprism.fishing.domain.repository.RestoreRepository
import com.mobileprism.fishing.domain.use_cases.validation.ValidationResult
import com.mobileprism.fishing.domain.use_cases.validation.ValidationUseCase
import com.mobileprism.fishing.model.utils.fold
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class SearchAccountViewModel(
    private val validationUseCase: ValidationUseCase,
    private val restoreRepository: RestoreRepository
) : ViewModel() {

    private val _searchState = MutableStateFlow<UiState?>(null)
    val searchState = _searchState.asStateFlow()

    private val _confirmState = MutableStateFlow<BaseViewState<UserLogin>?>(null)
    val confirmState = _confirmState.asStateFlow()

    private val _restoreInfo = MutableStateFlow(RestoreInfo())
    val restoreInfo = _restoreInfo.asStateFlow()

    private var searchJob: Job? = null
    private var confirmJob: Job? = null

    fun setLogin(login: String) {
        _restoreInfo.update {
            _restoreInfo.value.copy(
                login = login, loginError = ValidationResult(true)
            )
        }
    }

    fun onOtpSet(otp: String) {
        if (otp.isEmpty() || (otp.length <= ValidationUseCase.OTP_LENGTH && otp.toIntOrNull() != null)) {
            _restoreInfo.update {
                _restoreInfo.value.copy(
                    otp = otp, otpError = ValidationResult(true)
                )
            }
        }

    }

    fun validateLogin(skipEmpty: Boolean = false) {
        if (skipEmpty && _restoreInfo.value.login.isEmpty()) return

        restoreInfo.value.apply {
            _restoreInfo.update {
                it.copy(loginError = validationUseCase.validateLogin(login))
            }
        }
    }

    fun validateOtp(skipEmpty: Boolean = false) {
        if (skipEmpty && _restoreInfo.value.otp.isEmpty()) return

        restoreInfo.value.apply {
            _restoreInfo.update {
                it.copy(otpError = validationUseCase.validateOTP(otp))
            }
        }
    }

    fun cancelSearch() {
        searchJob?.cancel()
        resetSearchState()
    }

    fun searchAccount() {
        searchJob = viewModelScope.launch {
            restoreInfo.value.apply {

                val loginResult = validationUseCase.validateLogin(login)
                val hasError = loginResult.successful.not()



                _restoreInfo.update {
                    it.copy(
                        loginError = loginResult,
                    )
                }

                if (hasError) return@launch

                _searchState.update { UiState.InProgress }
                restoreRepository.searchAccount(RestoreRemoteFind(restoreInfo.value.login)).single()
                    .fold(onSuccess = {
                        _searchState.update { UiState.Success }
                    }, onError = {
                        _searchState.update { UiState.Error }
                    })
            }
        }
    }

    fun cancelConfirm() {
        confirmJob?.cancel()
        resetConfirmState()
    }

    fun confirmAccount() {
        confirmJob = viewModelScope.launch {
            restoreInfo.value.apply {

                val loginResult = validationUseCase.validateLogin(login)
                val otpResult = validationUseCase.validateOTP(otp)
                val hasError = listOf(loginResult, otpResult).any { it.successful.not() }

                _restoreInfo.update {
                    it.copy(
                        loginError = loginResult,
                        otpError = otpResult
                    )
                }

                if (hasError) return@launch

                _confirmState.update { BaseViewState.Loading() }
                restoreRepository.confirmOTP(RestoreRemoteConfirm(login, otp.toIntOrNull() ?: 0))
                    .single().fold(onSuccess = {
                        if (it.success)
                            _confirmState.update { BaseViewState.Success(UserLogin(login = login)) }
                        else _confirmState.update { BaseViewState.Error(null) }
                    }, onError = {
                        _confirmState.update { BaseViewState.Error(null) }
                    })
            }
        }
    }

    private fun resetSearchState() {
        _searchState.update { null }
    }

    private fun resetConfirmState() {
        _confirmState.update { null }
    }

    fun resetStates() {
        resetSearchState()
        resetConfirmState()
    }


}

@Parcelize
data class UserLogin(
    val login: String,
) : Parcelable

data class RestoreInfo(
    val login: String = "",
    val loginError: ValidationResult = ValidationResult(true),
    val otp: String = "",
    val otpError: ValidationResult = ValidationResult(true)
)

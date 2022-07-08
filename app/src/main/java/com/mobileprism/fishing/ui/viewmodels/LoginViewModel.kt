package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.common.LoginPassword
import com.mobileprism.fishing.domain.use_cases.users.RegisterNewUserUseCase
import com.mobileprism.fishing.domain.use_cases.users.SignInUserUserCase
import com.mobileprism.fishing.domain.use_cases.users.SignInUserWithGoogleUseCase
import com.mobileprism.fishing.domain.use_cases.users.SkipAuthorizationUseCase
import com.mobileprism.fishing.ui.viewstates.LoginScreenViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val registerNewUserUseCase: RegisterNewUserUseCase,
    private val signInUserUseCase: SignInUserUserCase,
    private val signInUserWithGoogleUseCase: SignInUserWithGoogleUseCase,
    private val skipAuthorizationUseCase: SkipAuthorizationUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<LoginScreenViewState> =
        MutableStateFlow(LoginScreenViewState.NotLoggedIn)
    val uiState = _uiState.asStateFlow()

    fun registerNewUser(loginPassword: LoginPassword) {
        _uiState.value = LoginScreenViewState.Loading

        viewModelScope.launch {
            registerNewUserUseCase(loginPassword).fold(
                onSuccess = {
                    _uiState.value = LoginScreenViewState.LoginSuccess
                },
                onFailure = {
                    handleError(it)
                }
            )
        }
    }

    fun signInUser(loginPassword: LoginPassword) {
        _uiState.value = LoginScreenViewState.Loading

        viewModelScope.launch {
            signInUserUseCase(loginPassword).fold(
                onSuccess = {
                    _uiState.value = LoginScreenViewState.LoginSuccess
                },
                onFailure = {
                    handleError(it)
                }
            )
        }
    }

    fun skipAuthorization() {
        _uiState.value = LoginScreenViewState.Loading

        viewModelScope.launch {
            skipAuthorizationUseCase().fold(
                onSuccess = {
                    _uiState.value = LoginScreenViewState.LoginSuccess
                },
                onFailure = {
                    handleError(it)
                }
            )
        }
    }

//    private fun loadCurrentUser() {
////        viewModelScope.launch {
////            firebaseRepository.currentUser
////                .catch { error -> handleError(error) }
////                .collectLatest { user -> user?.let { onSuccess(it) } }
////        }
//    }
//
//    private fun onSuccess(user: User) {
////        viewModelScope.launch {
////
////            firebaseRepository.addNewUser(user).collect { progress ->
////                when (progress) {
////                    is Progress.Complete -> {
////                        userDatastore.saveUser(user)
////                        _uiState.value = BaseViewState.Success(user)
////                    }
////                    is Progress.Loading -> {
////                        _uiState.value = BaseViewState.Loading(null)
////                    }
////                    is Progress.Error -> {
////                        _uiState.value = BaseViewState.Error(progress.error)
////                    }
////                }
////            }
////        }
//    }

    private fun handleError(error: Throwable) {
        _uiState.value = LoginScreenViewState.Error(error)
    }

    fun continueWithGoogle() {
        _uiState.value = LoginScreenViewState.Loading

        viewModelScope.launch {
            signInUserWithGoogleUseCase().fold(
                onSuccess = {
                    _uiState.value = LoginScreenViewState.LoginSuccess
                },
                onFailure = {
                    handleError(it)
                }
            )
        }
    }

}



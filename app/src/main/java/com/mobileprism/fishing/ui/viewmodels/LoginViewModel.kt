package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.common.LoginPassword
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.use_cases.users.RegisterNewUserUseCase
import com.mobileprism.fishing.domain.use_cases.users.SignInUserUserCase
import com.mobileprism.fishing.domain.use_cases.users.SignInUserWithGoogleUseCase
import com.mobileprism.fishing.domain.use_cases.users.SkipAuthorizationUseCase
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val registerNewUserUseCase: RegisterNewUserUseCase,
    private val signInUserUseCase: SignInUserUserCase,
    private val signInUserWithGoogleUseCase: SignInUserWithGoogleUseCase,
    private val skipAuthorizationUseCase: SkipAuthorizationUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<BaseViewState<User?>> =
        MutableStateFlow(BaseViewState.Success<User?>(null))
    val uiState = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    fun registerNewUser(loginPassword: LoginPassword) {
        _uiState.value = BaseViewState.Loading(null)

        viewModelScope.launch {
            registerNewUserUseCase(loginPassword).fold(
                onSuccess = {

                },
                onFailure = {

                }
            )
        }
    }

    fun signInUser(loginPassword: LoginPassword) {
        _uiState.value = BaseViewState.Loading(null)

        viewModelScope.launch {
            signInUserUseCase(loginPassword).fold(
                onSuccess = {

                },
                onFailure = {

                }
            )
        }
    }

    fun skipAuthorization() {
        _uiState.value = BaseViewState.Loading(null)

        viewModelScope.launch {
            skipAuthorizationUseCase().fold(
                onSuccess = {

                },
                onFailure = {

                }
            )
        }
    }

    private fun loadCurrentUser() {
//        viewModelScope.launch {
//            firebaseRepository.currentUser
//                .catch { error -> handleError(error) }
//                .collectLatest { user -> user?.let { onSuccess(it) } }
//        }
    }

    private fun onSuccess(user: User) {
//        viewModelScope.launch {
//
//            firebaseRepository.addNewUser(user).collect { progress ->
//                when (progress) {
//                    is Progress.Complete -> {
//                        userDatastore.saveUser(user)
//                        _uiState.value = BaseViewState.Success(user)
//                    }
//                    is Progress.Loading -> {
//                        _uiState.value = BaseViewState.Loading(null)
//                    }
//                    is Progress.Error -> {
//                        _uiState.value = BaseViewState.Error(progress.error)
//                    }
//                }
//            }
//        }
    }

    private fun handleError(error: Throwable) {
        _uiState.value = BaseViewState.Error(error)
    }

    fun continueWithGoogle() {
        _uiState.value = BaseViewState.Loading(null)

        viewModelScope.launch {
            signInUserWithGoogleUseCase().fold(
                onSuccess = {

                },
                onFailure = {

                }
            )
        }
    }

}

package com.mobileprism.fishing.ui.viewmodels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.auth.GoogleAuthRequest
import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.model.auth.LoginState
import com.mobileprism.fishing.model.utils.fold
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.network.ConnectionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StartViewModel(
    private val authManager: AuthManager,
    private val connectionManager: ConnectionManager
) : ViewModel() {

    private val _uiState: MutableStateFlow<BaseViewState<Unit>?> =
        MutableStateFlow(null)
    val uiState = _uiState.asStateFlow()

/*    init {
        subscribeOnLoginState()
    }*/
/*
    private fun subscribeOnLoginState() {
        viewModelScope.launch {
            authManager.loginState.collectLatest {
                when (it) {
                    // TODO: Deal with LoginScreenViewState mess
                    is LoginState.LoggedIn -> {
                        _uiState.value = LoginScreenViewState.LoginSuccess
                    }
                    is LoginState.LoginFailure -> {
                        _uiState.value = (LoginScreenViewState.NotLoggedIn)
                        // TODO:
                        //handleError(it.fishingResponse)
                    }
                    LoginState.NotLoggedIn -> {
                        _uiState.value = (LoginScreenViewState.NotLoggedIn)
                    }
                }
            }
        }
    }*/


    fun continueWithGoogle(account: GoogleSignInAccount, firebaseUser: FirebaseUser?) {
        viewModelScope.launch {
            _uiState.update { BaseViewState.Loading }
            if (account.email != null && account.idToken != null && account.id != null && firebaseUser?.uid != null) {
                authManager.googleLogin(account.email!!, account.id!!, account.idToken!!, firebaseUser.uid).single().fold(
                    onSuccess = {
                        _uiState.update { BaseViewState.Success(Unit) }
                    },
                    onError = {
                        _uiState.update { BaseViewState.Error() }
                    }
                )
            } else {
                _uiState.update { BaseViewState.Error() }
            }
        }
    }

    fun googleAuthError(exception: Exception) {
        _uiState.update { BaseViewState.Error() }
        // TODO:  
        /*handleError(
            FishingResponse(
                fishingCode = FishingCodes.UNKNOWN_ERROR,
                description = exception.message ?: ""
            )
        )*/
    }

    /*fun skipAuthorization() {
        _uiState.update { LoginScreenViewState.Loading }

        viewModelScope.launch {
            authManager.skipAuthorization()
        }
    }*/

    /*private fun handleError(error: Throwable) {
        _uiState.update { LoginScreenViewState.Error(error) }
    }*/
}


package com.mobileprism.fishing.ui.viewmodels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.auth.GoogleAuthRequest
import com.mobileprism.fishing.domain.entity.common.FishingFirebaseUser
import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.model.auth.LoginState
import com.mobileprism.fishing.model.utils.fold
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.network.ConnectionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val FirebaseUser.toFishingFirebaseUser: FishingFirebaseUser
    get() = FishingFirebaseUser(
        uid = uid,
        email = email,
        displayName = displayName,
    )

class StartViewModel(
    private val authManager: AuthManager,
    private val connectionManager: ConnectionManager
) : ViewModel() {

    private val _uiState: MutableStateFlow<BaseViewState<Unit>?> =
        MutableStateFlow(null)
    val uiState = _uiState.asStateFlow()

    fun continueWithGoogle(account: GoogleSignInAccount, firebaseUser: FirebaseUser?) {
        viewModelScope.launch {
            _uiState.update { BaseViewState.Loading }

            if (account.email != null && account.idToken != null && account.id != null && firebaseUser != null) {
                authManager.googleLogin(account.email!!, account.id!!, account.idToken!!, firebaseUser.toFishingFirebaseUser).single().fold(
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


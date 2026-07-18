package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.ReauthRequiredException
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.app.OfflineRepository
import com.mobileprism.fishing.domain.use_cases.catches.GetUserCatchesUseCase
import com.mobileprism.fishing.model.datastore.UserDatastore
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.profile.findBestCatch
import com.mobileprism.fishing.ui.home.profile.findFavoritePlace
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.delete_account_error
import fishing.shared.generated.resources.sign_in_generic_error
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class DeleteAccountState {
    data object Idle : DeleteAccountState()
    data object InProgress : DeleteAccountState()
    data object ReauthRequired : DeleteAccountState()
}

class UserViewModel(
    private val userRepository: UserRepository,
    private val userDatastore: UserDatastore,
    private val repository: OfflineRepository,
    private val getUserCatchUseCase: GetUserCatchesUseCase
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User>(User())
    val currentUser = _currentUser.asStateFlow()

    private val _currentPlaces = MutableStateFlow<List<UserMapMarker>?>(null)
    val currentPlaces = _currentPlaces.asStateFlow()

    private val _currentCatches = MutableStateFlow<List<UserCatch>?>(null)
    val currentCatches = _currentCatches.asStateFlow()

    private val _bestCatch = MutableStateFlow<UserCatch?>(null)
    val bestCatch = _bestCatch.asStateFlow()

    private val _favoritePlace = MutableStateFlow<UserMapMarker?>(null)
    val favoritePlace = _favoritePlace.asStateFlow()

    private val _deleteAccountState = MutableStateFlow<DeleteAccountState>(DeleteAccountState.Idle)
    val deleteAccountState = _deleteAccountState.asStateFlow()

    val isAnonymous: StateFlow<Boolean> = userRepository.isAnonymous
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    init {
        getCurrentUser()
        getUserCatches()
        getUserPlaces()
    }

    private fun getCurrentUser() = viewModelScope.launch {
        userDatastore.getUser.collectLatest {
            _currentUser.value = it
        }
    }

    private fun getUserPlaces() = viewModelScope.launch {
        repository.getAllUserMarkersList().collectLatest {
            _currentPlaces.value = it
            _favoritePlace.value = findFavoritePlace(it)
        }
    }

    private fun getUserCatches() = viewModelScope.launch {
        getUserCatchUseCase().collectLatest {
            _currentCatches.value = it
            _bestCatch.value = findBestCatch(it)
        }
    }

    suspend fun logoutCurrentUser() {
        userRepository.logoutCurrentUser()
    }

    fun clearGuestData() {
        viewModelScope.launch {
            userRepository.clearGuestData()
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _deleteAccountState.value = DeleteAccountState.InProgress
            userRepository.deleteAccount()
                .onSuccess { _deleteAccountState.value = DeleteAccountState.Idle }
                .onFailure { handleDeleteAccountError(it) }
        }
    }

    fun reauthenticateAndDeleteAccount(idToken: String) {
        viewModelScope.launch {
            _deleteAccountState.value = DeleteAccountState.InProgress
            userRepository.reauthenticateWithGoogle(idToken)
                .onSuccess {
                    userRepository.deleteAccount()
                        .onSuccess { _deleteAccountState.value = DeleteAccountState.Idle }
                        .onFailure { handleDeleteAccountError(it) }
                }
                .onFailure { handleDeleteAccountError(it) }
        }
    }

    fun cancelDeleteAccount() {
        _deleteAccountState.value = DeleteAccountState.Idle
    }

    fun onReauthSignInFailed() {
        _deleteAccountState.value = DeleteAccountState.Idle
        SnackbarManager.showMessage(Res.string.sign_in_generic_error)
    }

    private fun handleDeleteAccountError(error: Throwable) {
        if (error is ReauthRequiredException) {
            _deleteAccountState.value = DeleteAccountState.ReauthRequired
        } else {
            _deleteAccountState.value = DeleteAccountState.Idle
            SnackbarManager.showMessage(Res.string.delete_account_error)
        }
    }
}

package com.mobileprism.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.ui.home.profile.findBestCatch
import com.mobileprism.fishing.ui.home.profile.findFavoritePlace
import com.mobileprism.fishing.domain.viewstates.BaseViewState
import com.mobileprism.fishing.model.entity.common.User
import com.mobileprism.fishing.model.entity.content.UserCatch
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.repository.UserRepository
import com.mobileprism.fishing.model.repository.app.OfflineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository,
    private val repository: OfflineRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _currentPlaces = MutableStateFlow<List<UserMapMarker>?>(null)
    val currentPlaces = _currentPlaces.asStateFlow()

    private val _currentCatches = MutableStateFlow<List<UserCatch>?>(null)
    val currentCatches = _currentCatches.asStateFlow()

    private val _bestCatch = MutableStateFlow<UserCatch?>(null)
    val bestCatch = _bestCatch.asStateFlow()

    private val _favoritePlace = MutableStateFlow<UserMapMarker?>(null)
    val favoritePlace = _favoritePlace.asStateFlow()

    init {
        getCurrentUser()
        getUserCatches()
        getUserPlaces()
    }

    private val _uiState = MutableStateFlow<BaseViewState>(BaseViewState.Success(null))
    val uiState: StateFlow<BaseViewState>
        get() = _uiState

    private fun getCurrentUser() = viewModelScope.launch {
        userRepository.datastoreUser.collect {
            _currentUser.value = it
        }
    }

    private fun getUserPlaces() = viewModelScope.launch {
        repository.getAllUserMarkersList().collect {
            if (it.isEmpty()) {
                _currentCatches.value = listOf()
            }
            _currentPlaces.value = it
            _favoritePlace.value = findFavoritePlace(it)
        }
    }

    private fun getUserCatches() = viewModelScope.launch {
        repository.getAllUserCatchesList().collect {
            _currentCatches.value = it
            _bestCatch.value = findBestCatch(it)
        }
    }

    suspend fun logoutCurrentUser() = viewModelScope.run {
        userRepository.logoutCurrentUser()
    }

}



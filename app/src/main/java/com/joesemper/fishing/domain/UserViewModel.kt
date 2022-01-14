package com.joesemper.fishing.domain

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.compose.ui.home.profile.findBestCatch
import com.joesemper.fishing.compose.ui.home.profile.findFavoritePlace
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.repository.UserRepository
import com.joesemper.fishing.model.repository.app.OfflineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository,
    private val repository: OfflineRepository
) : ViewModel() {

    val currentUser = mutableStateOf<User?>(null)

    val currentPlaces = mutableStateOf<List<UserMapMarker>?>(null)
    val currentCatches = mutableStateOf<List<UserCatch>?>(null)
    val bestCatch = mutableStateOf<UserCatch?>(null)
    val favoritePlace = mutableStateOf<UserMapMarker?>(null)

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
            currentUser.value = it
        }
    }


    private fun getUserPlaces() = viewModelScope.launch {
        repository.getAllUserMarkersList().collect {
            if (it.isEmpty()) {
                currentCatches.value = listOf()
            }
            currentPlaces.value = it
            favoritePlace.value = findFavoritePlace(it)
        }
    }


    private fun getUserCatches() = viewModelScope.launch {
        repository.getAllUserCatchesList().collect {
            currentCatches.value = it
            bestCatch.value = findBestCatch(it)
        }
    }


    suspend fun logoutCurrentUser() = viewModelScope.run {
        userRepository.logoutCurrentUser()
    }

}



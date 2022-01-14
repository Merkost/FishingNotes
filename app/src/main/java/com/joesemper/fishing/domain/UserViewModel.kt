package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    val currentUser = MutableStateFlow<User?>(null)
    val currentPlaces = MutableStateFlow<List<UserMapMarker>?>(null)
    val currentCatches = MutableStateFlow<List<UserCatch>?>(null)

    init {
        getCurrentUser()
        getUserCatches()
        getUserPlaces()
    }

    private val _uiState = MutableStateFlow<BaseViewState>(BaseViewState.Success(null))
    val uiState: StateFlow<BaseViewState>
        get() = _uiState

    private fun getCurrentUser() = viewModelScope.run {
        viewModelScope.launch {
            userRepository.datastoreUser.collect {
                currentUser.value = it
            }
        }
    }

    private fun getUserPlaces() = viewModelScope.run {
        viewModelScope.launch {
            repository.getAllUserMarkersList().collect {
                if (it.isEmpty()) {
                    currentCatches.value = listOf()
                }
                currentPlaces.value = it
            }
        }
    }

    private fun getUserCatches() = viewModelScope.run {
        viewModelScope.launch {
            repository.getAllUserCatchesList().collect {
                currentCatches.value = it
            }
        }
    }

    suspend fun logoutCurrentUser() = viewModelScope.run {
        userRepository.logoutCurrentUser()
    }

}



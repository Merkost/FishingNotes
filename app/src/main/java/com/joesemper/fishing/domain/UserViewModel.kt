package com.joesemper.fishing.domain

import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.UserRepository
import com.joesemper.fishing.utils.getCurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository,
    private val repository: UserContentRepository
) : ViewModel() {


    private val _uiState = MutableStateFlow<BaseViewState>(BaseViewState.Success(null))
    val uiState: StateFlow<BaseViewState>
        get() = _uiState

    fun getCurrentUser() = userRepository.currentUser

    fun getUserPlaces() = repository.getAllUserMarkersList()

    fun getUserCatches() = repository.getAllUserCatchesList()

    suspend fun logoutCurrentUser() = userRepository.logoutCurrentUser()

}
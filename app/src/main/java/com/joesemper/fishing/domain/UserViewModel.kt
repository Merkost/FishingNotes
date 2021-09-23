package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel(
    private val userRepository: UserRepository,
    private val repository: UserContentRepository
) : ViewModel() {

    init {
        getUserCatches()
    }

    private val _uiState = MutableStateFlow<BaseViewState>(BaseViewState.Success(null))
    val uiState: StateFlow<BaseViewState>
        get() = _uiState

    fun getCurrentUser() = userRepository.currentUser

    fun getUserPlaces() = repository.getAllUserMarkersList()

    fun getUserCatches() = repository.getAllUserCatchesList()


    suspend fun logoutCurrentUser() = userRepository.logoutCurrentUser()


}
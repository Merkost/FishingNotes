package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow

class UserViewModel(private val userRepository: UserRepository,
                    private val repository: UserContentRepository): ViewModel() {

    private val mutableStateFlow: MutableStateFlow<BaseViewState> =
        MutableStateFlow(BaseViewState.Success(null))

    fun getCurrentUser() = userRepository.currentUser

    fun getUserPlaces() = repository.getAllUserMarkersList()

    fun getUserCatches() = repository.getAllUserCatchesList()

    suspend fun logoutCurrentUser() = userRepository.logoutCurrentUser()

}
package com.joesemper.fishing.domain

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserPlaceViewModel(
    private val userRepository: UserRepository,
    private val repository: UserContentRepository,
) : ViewModel() {

    val marker: MutableState<UserMapMarker?> = mutableStateOf(null)

    fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>> {
        return viewModelScope.run {
            repository.getCatchesByMarkerId(markerId)
        }
    }

    fun deletePlace() {
        viewModelScope.launch {
            marker.value?.let {
                repository.deleteMarker(it)
            }

        }
    }

}
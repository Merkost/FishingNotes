package com.joesemper.fishing.domain

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.UserRepository
import kotlinx.coroutines.launch

class UserPlaceViewModel(
    private val userRepository: UserRepository,
    private val repository: UserContentRepository,
) : ViewModel() {

    val marker: MutableState<UserMapMarker> = mutableStateOf(UserMapMarker())

    var titleTemp = mutableStateOf("")
    var descriptionTemp = mutableStateOf("")

    var title = marker.value.title
    var description = marker.value.description

    fun save() {
        marker.value.title = titleTemp.value
        marker.value.description = descriptionTemp.value
    }

    fun getCatchesByMarkerId(markerId: String) = repository.getCatchesByMarkerId(markerId)

    fun getCurrentUser() = userRepository.currentUser

    fun deletePlace(place: UserMapMarker) {
        viewModelScope.launch {
            repository.deleteMarker(place)
        }
    }

}
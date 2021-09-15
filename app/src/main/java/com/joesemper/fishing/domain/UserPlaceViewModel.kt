package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.UserRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserPlaceViewModel(private val userRepository: UserRepository, private val repository: UserContentRepository): ViewModel() {


    fun getCatchesByMarkerId(markerId: String) = repository.getCatchesByMarkerId(markerId)

    fun getCurrentUser() = userRepository.currentUser

    fun deletePlace(place: UserMapMarker) {
        viewModelScope.launch {
            repository.deleteMarker(place)
        }
    }

}
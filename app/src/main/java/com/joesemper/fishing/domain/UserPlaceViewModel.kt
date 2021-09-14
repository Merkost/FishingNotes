package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.UserRepository

class UserPlaceViewModel(private val userRepository: UserRepository, private val repository: UserContentRepository): ViewModel() {


    fun getCatchesByMarkerId(markerId: String) = repository.getCatchesByMarkerId(markerId)
    //fun getMapMarker(markerId: String) = repository.getMapMarker(markerId)
    fun getCurrentUser() = userRepository.currentUser

}
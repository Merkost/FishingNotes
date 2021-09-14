package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.UserRepository

class UserCatchViewModel(private val repository: UserContentRepository): ViewModel() {



    fun getMapMarker(markerId: String) = repository.getMapMarker(markerId)

}
package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import com.joesemper.fishing.model.repository.UserContentRepository

class UserPlaceViewModel(private val repository: UserContentRepository): ViewModel() {
    fun getMapMarker(markerId: String) = repository.getMapMarker(markerId)
}
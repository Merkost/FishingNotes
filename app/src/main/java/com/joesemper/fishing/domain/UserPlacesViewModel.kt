package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.repository.UserContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserPlacesViewModel(private val repository: UserContentRepository) : ViewModel() {

    private val places = mutableListOf<UserMapMarker>()

    val currentContent = MutableStateFlow<List<UserMapMarker>>(places)

    init {
        loadAllUserPlaces()
    }

    private fun loadAllUserPlaces() {
        viewModelScope.launch {
            repository.getAllUserMarkersList().collect { userPlaces ->
                places.clear()
                places.addAll(userPlaces as List<UserMapMarker>)
            }
        }
    }
}
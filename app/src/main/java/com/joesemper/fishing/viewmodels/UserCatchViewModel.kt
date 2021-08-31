package com.joesemper.fishing.viewmodels

import androidx.lifecycle.ViewModel
import com.joesemper.fishing.data.repository.UserContentRepository

class UserCatchViewModel(private val repository: UserContentRepository): ViewModel() {

    fun getMapMarker(markerId: String) = repository.getMapMarker(markerId)

}
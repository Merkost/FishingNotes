package com.joesemper.fishing.viewmodels

import androidx.lifecycle.ViewModel
import com.joesemper.fishing.data.repository.UserCatchRepository

class UserCatchViewModel(private val repository: UserCatchRepository): ViewModel() {

    fun getMapMarker(markerId: String) = repository.getMapMarker(markerId)

}
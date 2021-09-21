package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.UserRepository
import kotlinx.coroutines.launch

class UserCatchViewModel(private val repository: UserContentRepository,
                         private val userRepository: UserRepository): ViewModel() {

    fun deleteCatch(catch: UserCatch) {
        viewModelScope.launch {
            repository.deleteCatch(catch)
        }
    }

    fun getCurrentUser() = userRepository.currentUser

    fun getMapMarker(markerId: String) = repository.getMapMarker(markerId)

    //    private fun setInitialData() {
//        lifecycleScope.launchWhenStarted {
//            viewModel.getMapMarker(catch.userMarkerId).collect { marker ->
//                if (marker != null) {
//                    binding.tvPlaceTitle.text = marker.title
//                    binding.tvPlaceDescription.text = marker.description
//                }
//            }
//        }
//
//    }
}
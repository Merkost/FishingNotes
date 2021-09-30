package com.joesemper.fishing.domain

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.entity.content.MapMarker
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.UserRepository
import kotlinx.coroutines.launch

class UserCatchViewModel(private val repository: UserContentRepository,
                         private val userRepository: UserRepository): ViewModel() {

    val catch: MutableState<UserCatch> = mutableStateOf(UserCatch())
    //val mapMarker: UserMapMarker =
     //   if (catch.value.userMarkerId.isNotBlank()) getMapMarker(catch.value.userMarkerId)
      //  else UserMapMarker()

    fun deleteCatch() {
            viewModelScope.launch {
                repository.deleteCatch(catch.value)
            }
    }

    fun getCurrentUser() = userRepository.currentUser

    fun getMapMarker(markerId: String) = repository.getMapMarker(catch.value.userMarkerId)

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
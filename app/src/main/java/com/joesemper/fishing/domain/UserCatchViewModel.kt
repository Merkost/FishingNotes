package com.joesemper.fishing.domain

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.maps.model.Marker
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.UserRepository
import com.joesemper.fishing.model.repository.app.CatchesRepository
import com.joesemper.fishing.model.repository.app.MarkersRepository
import kotlinx.coroutines.launch

class UserCatchViewModel(
    private val markersRepo: MarkersRepository,
    private val catchesRepo: CatchesRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    val catch: MutableState<UserCatch?> = mutableStateOf(null)

    fun deleteCatch() {
        viewModelScope.launch {
            catch.value?.let {
                catchesRepo.deleteCatch(it)
            }
        }
    }

    fun getCurrentUser() = userRepository.currentUser

    fun getMapMarker(markerId: String) = markersRepo.getMapMarker(markerId)
}
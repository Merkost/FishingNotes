package com.joesemper.fishing.domain

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.repository.UserRepository
import com.joesemper.fishing.model.repository.app.CatchesRepository
import com.joesemper.fishing.model.repository.app.MarkersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserCatchViewModel(
    private val markersRepository: MarkersRepository,
    private val catchesRepository: CatchesRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _catch = MutableStateFlow<UserCatch?>(null)
    val catch = _catch.asStateFlow()

    private val _mapMarker = MutableStateFlow<UserMapMarker?>(null)
    val mapMarker = _mapMarker.asStateFlow()

    private val _loadingState = MutableStateFlow<Progress>(Progress.Complete)
    val loadingState = _loadingState.asStateFlow()

    fun setCatch(userCatch: UserCatch) {
        _catch.value = userCatch
    }

    fun updateCatch(data: Map<String, Any>) {
        mapMarker.value?.let { marker ->
            catch.value?.let { catch ->
                viewModelScope.launch {
                    catchesRepository.updateUserCatch(
                        markerId = marker.id,
                        catchId = catch.id,
                        data = data
                    )
                }
            }
        }
    }

    fun deleteCatch() {
        viewModelScope.launch {
            catch.value?.let {
                catchesRepository.deleteCatch(it)
            }
        }
    }

    fun getCurrentUser() = userRepository.currentUser

    fun getMapMarker(markerId: String) {
        viewModelScope.launch {
            markersRepository.getMapMarker(markerId).collect {
                _mapMarker.value = it
                subscribeOnCatchChanges()
            }
        }
    }

    fun updateCatchPhotos(photos: List<Uri>) {
        mapMarker.value?.let { marker ->
            catch.value?.let { catch ->
                viewModelScope.launch {
                    _loadingState.value = Progress.Loading()
                    catchesRepository.updateUserCatchPhotos(
                        markerId = marker.id,
                        catchId = catch.id,
                        newPhotos = photos
                    ).collect {
                        _loadingState.value = it
                    }
                }
            }
        }
    }

    private suspend fun subscribeOnCatchChanges() {
        mapMarker.value?.let { marker ->
            catch.value?.let { oldCatch ->
                catchesRepository.subscribeOnUserCatchState(
                    markerId = marker.id,
                    catchId = oldCatch.id
                ).collect {
                    _catch.value = it
                }
            }
        }
    }

}
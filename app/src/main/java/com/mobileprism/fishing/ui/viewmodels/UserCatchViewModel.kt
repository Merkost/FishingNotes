package com.mobileprism.fishing.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.common.Progress
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.use_cases.catches.DeleteUserCatchUseCase
import com.mobileprism.fishing.domain.use_cases.places.GetMapMarkerByIdUseCase
import com.mobileprism.fishing.domain.use_cases.SubscribeOnUserCatchStateUseCase
import com.mobileprism.fishing.domain.use_cases.catches.UpdateUserCatchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserCatchViewModel(
    userCatch: UserCatch,
    private val updateUserCatch: UpdateUserCatchUseCase,
    private val deleteUserCatch: DeleteUserCatchUseCase,
    private val getMapMarkerById: GetMapMarkerByIdUseCase,
    private val subscribeOnUserCatchState: SubscribeOnUserCatchStateUseCase
) : ViewModel() {

    private val _catch = MutableStateFlow(userCatch)
    val catch = _catch.asStateFlow()

    private val _mapMarker = MutableStateFlow<UserMapMarker?>(null)
    val mapMarker = _mapMarker.asStateFlow()

    private val _loadingState = MutableStateFlow<Progress>(Progress.Complete)
    val loadingState = _loadingState.asStateFlow()

    init {
        getMapMarker(userCatch.userMarkerId)
    }

    fun updateCatchInfo(fishType: String, fishAmount: Int, fishWeight: Double) {
        updateCatch(
            catch.value.copy(
                fishType = fishType,
                fishAmount = fishAmount,
                fishWeight = fishWeight
            )
        )
    }

    fun updateNote(note: Note) {
        updateCatch(
            catch.value.copy(
                note = note
            )
        )
    }

    fun updateWayOfFishing(fishingRodType: String, fishingLure: String, fishingBait: String) {
        updateCatch(
            catch.value.copy(
                fishingRodType = fishingRodType,
                fishingLure = fishingLure,
                fishingBait = fishingBait
            )
        )
    }

    fun updateCatchPhotos(photos: List<Uri>) {
        updateCatch(
            catch.value.copy(
                downloadPhotoLinks = photos.map { it.toString() }
            )
        )
    }

    private fun updateCatch(newCatch: UserCatch) {
        viewModelScope.launch { updateUserCatch(newCatch = newCatch) }
    }

    fun deleteCatch() {
        viewModelScope.launch {
            deleteUserCatch(catch.value)
        }
    }

    private fun getMapMarker(markerId: String) {
        viewModelScope.launch {
            getMapMarkerById(markerId).fold(
                onSuccess = {
                    _mapMarker.value = it
                    subscribeOnCatchChanges()
                },
                onFailure = { }
            )

        }
    }

    private suspend fun subscribeOnCatchChanges() {
        mapMarker.value?.let { marker ->
            catch.value.let { oldCatch ->
                subscribeOnUserCatchState(
                    markerId = marker.id,
                    catchId = oldCatch.id
                ).collect {
                    _catch.value = it
                }
            }
        }
    }

}
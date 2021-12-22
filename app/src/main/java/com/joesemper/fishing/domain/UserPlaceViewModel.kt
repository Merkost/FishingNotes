package com.joesemper.fishing.domain

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.SnackbarManager
import com.joesemper.fishing.model.entity.common.LiteProgress
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.repository.app.CatchesRepository
import com.joesemper.fishing.model.repository.app.MarkersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserPlaceViewModel(
    private val markersRepo: MarkersRepository,
    private val catchesRepo: CatchesRepository,
) : ViewModel() {

    var markerVisibility: MutableState<Boolean?> = mutableStateOf(null)
    val marker: MutableState<UserMapMarker?> = mutableStateOf(null)

    fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>> {
        return viewModelScope.run {
            catchesRepo.getCatchesByMarkerId(markerId)
        }
    }

    fun deletePlace() {
        viewModelScope.launch {
            marker.value?.let {
                markersRepo.deleteMarker(it)
            }

        }
    }

    fun changeVisibility(newIsVisible: Boolean) {
        viewModelScope.launch {
            marker.value?.let {
                markerVisibility.value = newIsVisible
                markersRepo.changeMarkerVisibility(it, changeTo = newIsVisible).collect {
                    when (it) {
                        is LiteProgress.Loading -> {}
                        is LiteProgress.Complete -> {
                            SnackbarManager.showMessage(R.string.marker_visibility_change_success)
                        }
                        is LiteProgress.Error -> {
                            markerVisibility.value = !newIsVisible
                            SnackbarManager.showMessage(R.string.marker_visibility_change_error)
                        }
                    }
                }
            }

        }
    }

}
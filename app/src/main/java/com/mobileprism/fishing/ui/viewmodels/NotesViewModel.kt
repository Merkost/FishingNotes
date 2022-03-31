package com.mobileprism.fishing.ui.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.use_cases.GetUserCatchesByMarkerId
import com.mobileprism.fishing.domain.use_cases.GetUserPlacesListUseCase
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotesViewModel(
    private val getUserPlacesList: GetUserPlacesListUseCase,
    private val getUserCatches: GetUserCatchesByMarkerId
) : ViewModel() {

    private val userPlacesList = mutableStateListOf<PlaceNoteItemUiState>()

    private val _uiState =
        MutableStateFlow<BaseViewState<List<PlaceNoteItemUiState>>>(BaseViewState.Loading())
    val uiState = _uiState.asStateFlow()

    init {
        getAllUserPlaces()
    }

    private fun getAllUserPlaces() {
        viewModelScope.launch {
            getUserPlacesList().collect { markers ->
                userPlacesList.clear()
                markers.forEach { userPlacesList.add(PlaceNoteItemUiState(place = it)) }

                _uiState.value = BaseViewState.Success(userPlacesList)
            }
        }
    }

    fun onPlaceExpandItemClick(marker: UserMapMarker) {
        viewModelScope.launch {
            userPlacesList.find { it.place.id == marker.id }?.let { item ->
                val index = userPlacesList.indexOf(item)

                userPlacesList[index] = item.copy(isExpanded = !userPlacesList[index].isExpanded)

                if (item.catchesState is NoteCatchesState.Loading) {

                    getUserCatches(marker.id).fold(
                        onSuccess = {
                            userPlacesList[index] =
                                item.copy(catchesState = NoteCatchesState.Loaded(it))
                        },
                        onFailure = {
                            _uiState.value = BaseViewState.Error(it)
                        }
                    )
                }
            }
        }
    }

}


data class PlaceNoteItemUiState(
    val place: UserMapMarker,
    val isExpanded: Boolean = false,
    val catchesState: NoteCatchesState = NoteCatchesState.Loading,
)

sealed class NoteCatchesState() {
    object Loading : NoteCatchesState()
    class Loaded(val catches: List<UserCatch>) : NoteCatchesState()
}

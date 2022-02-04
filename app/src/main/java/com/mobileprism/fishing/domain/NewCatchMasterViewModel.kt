package com.mobileprism.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.compose.ui.home.new_catch.NewCatchBuilder
import com.mobileprism.fishing.compose.ui.home.new_catch.NewCatchBuilderImpl
import com.mobileprism.fishing.compose.ui.home.new_catch.NewCatchPlacesState
import com.mobileprism.fishing.compose.ui.home.new_catch.ReceivedPlaceState
import com.mobileprism.fishing.domain.viewstates.BaseViewState
import com.mobileprism.fishing.model.entity.common.Progress
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.repository.app.CatchesRepository
import com.mobileprism.fishing.model.repository.app.MarkersRepository
import com.mobileprism.fishing.model.repository.app.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class NewCatchMasterViewModel(
    placeState: ReceivedPlaceState,
    private val markersRepository: MarkersRepository,
    private val catchesRepository: CatchesRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    init {
        getAllUserMarkersList()
    }

    private val calendar = Calendar.getInstance()

    private val builder: NewCatchBuilder = NewCatchBuilderImpl()

    val isLocationLocked = MutableStateFlow(placeState is ReceivedPlaceState.Received)
    val isPlaceInputCorrect = MutableStateFlow(true)

    val currentPlace = MutableStateFlow(
        if (placeState is ReceivedPlaceState.Received) placeState.place else null
    )

    private val _uiState = MutableStateFlow<BaseViewState>(BaseViewState.Success(null))
    val uiState = _uiState.asStateFlow()

    val markersListState = MutableStateFlow<NewCatchPlacesState>(NewCatchPlacesState.NotReceived)
    val catchDate = MutableStateFlow(calendar.timeInMillis)
    val fishType = MutableStateFlow("")
    val fishAmount = MutableStateFlow(0)
    val fishWeight = MutableStateFlow(0.0)
    val description = MutableStateFlow("")
    val rod = MutableStateFlow("")
    val bait = MutableStateFlow("")
    val lure = MutableStateFlow("")

    fun setSelectedPlace(place: UserMapMarker) {
        currentPlace.value = place
        builder.setPlaceId(place.id)
        builder.setPlaceTitle(place.title)
    }

    fun setPlaceInputError(isError: Boolean) {
        isPlaceInputCorrect.value = isError
    }

    fun setDate(date: Long) {
        catchDate.value = date
        builder.setDate(date)
    }

    fun setFishType(fish: String) {
        fishType.value = fish
        builder.setFishType(fish)
    }

    fun setFishAmount(amount: Int) {
        fishAmount.value = amount
        builder.setFishAmount(amount)
    }

    fun setFishWeight(weight: Double) {
        fishWeight.value = weight
        builder.setFishWeight(weight)
    }

    fun setNote(note: String) {
        description.value = note
        builder.setDescription(note)
    }

    fun setRod(rodValue: String) {
        rod.value = rodValue
        builder.setRodType(rodValue)
    }

    fun setBait(baitValue: String) {
        bait.value = baitValue
        builder.setBait(baitValue)
    }

    fun setLure(lureValue: String) {
        lure.value = lureValue
        builder.setLure(lureValue)
    }

    private fun getAllUserMarkersList() {
        viewModelScope.launch {
            markersRepository.getAllUserMarkersList().collect { markers ->
                markersListState.value =
                    NewCatchPlacesState.Received(markers as List<UserMapMarker>)
            }
        }
    }

    fun saveNewCatch() {
        _uiState.value = BaseViewState.Loading(0)
        val newCatch = builder.create()

        viewModelScope.launch {
            currentPlace.value?.let { userMapMarker ->
                catchesRepository.addNewCatch(userMapMarker.id, newCatch).collect { progress ->
                    when (progress) {
                        is Progress.Complete -> {
                            _uiState.value = BaseViewState.Success(progress)
                        }
                        is Progress.Loading -> {
                            _uiState.value = BaseViewState.Loading(progress.percents)
                        }
                        is Progress.Error -> {
                            _uiState.value = BaseViewState.Error(progress.error)
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        calendar.timeInMillis = Date().time
    }

}
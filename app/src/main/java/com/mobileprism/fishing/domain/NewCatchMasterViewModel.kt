package com.mobileprism.fishing.domain

import androidx.lifecycle.ViewModel
import com.mobileprism.fishing.compose.ui.home.new_catch.ReceivedPlaceState
import com.mobileprism.fishing.model.repository.app.CatchesRepository
import com.mobileprism.fishing.model.repository.app.MarkersRepository
import com.mobileprism.fishing.model.repository.app.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow

class NewCatchMasterViewModel(
    private val placeState: ReceivedPlaceState,
    private val markersRepository: MarkersRepository,
    private val catchesRepository: CatchesRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    val currentPlace = MutableStateFlow(
        if (placeState is ReceivedPlaceState.Received) {
            placeState.place
        } else {
            null
        }
    )

}
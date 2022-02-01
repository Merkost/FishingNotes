package com.mobileprism.fishing.domain

import androidx.lifecycle.ViewModel
import com.mobileprism.fishing.model.repository.app.CatchesRepository
import com.mobileprism.fishing.model.repository.app.MarkersRepository
import com.mobileprism.fishing.model.repository.app.WeatherRepository

class NewCatchMasterViewModel(
    private val markersRepository: MarkersRepository,
    private val catchesRepository: CatchesRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {


}
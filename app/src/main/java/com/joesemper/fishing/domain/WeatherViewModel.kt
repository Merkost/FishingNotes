package com.joesemper.fishing.domain

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.WeatherRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val contentRepository: UserContentRepository
) : ViewModel() {

    val markersList = mutableStateOf<MutableList<UserMapMarker>>(mutableListOf())
    val currentWeather = mutableStateOf<WeatherForecast?>(null)

    init {
        getAllMarkers()
    }

    private fun getAllMarkers() {
        viewModelScope.launch {
            contentRepository.getAllUserMarkersList().collect {
                markersList.value = it as MutableList<UserMapMarker>
            }
        }

    }

    fun getWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            weatherRepository.getWeather(latitude, longitude).collect {
                currentWeather.value = it
            }
        }
    }


}

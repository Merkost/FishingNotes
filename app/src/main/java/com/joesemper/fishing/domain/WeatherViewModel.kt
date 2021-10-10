package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.WeatherRepository

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val contentRepository: UserContentRepository
) : ViewModel() {

    fun getAllMarkers() = contentRepository.getAllUserMarkersList()

    fun getMarkerWeather(marker: UserMapMarker) =
        weatherRepository.getWeather(marker.latitude, marker.longitude)

    fun getWeather(latitude: Double, longitude: Double) =
        weatherRepository.getWeather(latitude, longitude)

}

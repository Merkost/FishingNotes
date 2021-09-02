package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.repository.WeatherRepository

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    fun getAllMarkers() = repository.getAllUserMarkersList()

    fun getMarkerWeather(marker: UserMapMarker) =
        repository.getWeather(marker.latitude, marker.longitude)

    fun getWeather(latitude: Double, longitude: Double) =
        repository.getWeather(latitude, longitude)

}
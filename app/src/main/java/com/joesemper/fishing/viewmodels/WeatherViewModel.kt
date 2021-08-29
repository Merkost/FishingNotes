package com.joesemper.fishing.viewmodels

import androidx.lifecycle.ViewModel
import com.joesemper.fishing.data.entity.content.UserMapMarker
import com.joesemper.fishing.data.repository.WeatherRepository

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    fun getAllMarkers() = repository.getAllUserMarkersList()

    fun getMarkerWeather(marker: UserMapMarker) =
        repository.getWeather(marker.latitude, marker.longitude)

    fun getWeather(latitude: Double, longitude: Double) =
        repository.getWeather(latitude, longitude)

}
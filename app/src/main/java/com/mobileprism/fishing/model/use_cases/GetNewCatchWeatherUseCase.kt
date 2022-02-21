package com.mobileprism.fishing.model.use_cases

import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.viewstates.Resource
import com.mobileprism.fishing.domain.viewstates.RetrofitWrapper
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.weather.WeatherForecast
import com.mobileprism.fishing.model.repository.app.WeatherRepository
import com.mobileprism.fishing.utils.isDateInList
import com.mobileprism.fishing.utils.isLocationsTooFar
import com.mobileprism.fishing.utils.time.hoursCount
import kotlinx.coroutines.flow.flow
import java.util.*

class GetNewCatchWeatherUseCase(val weatherRepository: WeatherRepository) {

    companion object {
        var lastLoadedWeather: WeatherForecast? = null
    }

    suspend operator fun invoke(place: UserMapMarker?, newCatchDate: Long) =
        flow<Resource<WeatherForecast>> {
            if (place == null) emit(Resource.Error(textRes = R.string.place_select_error))
            else {
                if (checkWeatherDownloadNeed(place, newCatchDate)) {
                    (if (Date().time.hoursCount() > newCatchDate.hoursCount()) {
                        getHistoricalWeather(place, newCatchDate)
                    } else { getWeatherForecast(place) }).collect { result ->
                        when (result) {
                            is RetrofitWrapper.Success<WeatherForecast> -> {
                                lastLoadedWeather = result.data
                                emit(Resource.Success(result.data))
                            }
                            is RetrofitWrapper.Error -> {
                                emit(Resource.Error(textRes = R.string.weather_error))
                            }
                            else -> {}
                        }
                    }
                } else lastLoadedWeather?.let { emit(Resource.Success(it)) }
            }
        }

    private suspend fun getWeatherForecast(place: UserMapMarker) =
        weatherRepository.getWeather(place.latitude, place.longitude)

    private suspend fun getHistoricalWeather(place: UserMapMarker, newCatchDate: Long) =
        weatherRepository.getHistoricalWeather(place.latitude, place.longitude, (newCatchDate / 1000))

    private fun checkWeatherDownloadNeed(place: UserMapMarker?, newCatchDate: Long): Boolean {
        if (lastLoadedWeather == null) return true

        val lastLoadedWeatherPlace = UserMapMarker(
            latitude = lastLoadedWeather!!.latitude.toDouble(),
            longitude = lastLoadedWeather!!.longitude.toDouble()
        )
        return place?.let { currentPlace ->
            isLocationsTooFar(currentPlace, lastLoadedWeatherPlace)
                    || !isDateInList(lastLoadedWeather!!.hourly, newCatchDate)
        } ?: false
    }
}
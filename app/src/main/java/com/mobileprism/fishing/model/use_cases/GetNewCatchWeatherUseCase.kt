package com.mobileprism.fishing.model.use_cases

import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.viewstates.Resource
import com.mobileprism.fishing.domain.viewstates.RetrofitWrapper
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.weather.WeatherForecast
import com.mobileprism.fishing.model.repository.app.WeatherRepository
import com.mobileprism.fishing.ui.home.new_catch.NewCatchWeatherData
import com.mobileprism.fishing.utils.calcMoonPhase
import com.mobileprism.fishing.utils.getClosestHourIndex
import com.mobileprism.fishing.utils.isDateInList
import com.mobileprism.fishing.utils.isLocationsTooFar
import com.mobileprism.fishing.utils.time.hoursCount
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.single
import java.util.*

class GetNewCatchWeatherUseCase(
    private val weatherRepository: WeatherRepository,
    private val weatherSettings: WeatherPreferences,
) {

    companion object {
        var lastLoadedWeather: WeatherForecast? = null
    }

    suspend operator fun invoke(place: UserMapMarker?, newCatchDate: Long) =
        flow<Resource<NewCatchWeatherData>> {
            if (place == null) emit(Resource.Error(textRes = R.string.place_select_error))
            else {
                if (checkWeatherDownloadNeed(place, newCatchDate)) {
                    (if (Date().time.hoursCount() > newCatchDate.hoursCount()) {
                        getHistoricalWeather(place, newCatchDate)
                    } else {
                        getWeatherForecast(place)
                    }).collect { result ->
                        when (result) {
                            is RetrofitWrapper.Success<WeatherForecast> -> {
                                lastLoadedWeather = result.data
                                emit(Resource.Success(mapToNewCatchWeather(result.data, newCatchDate)))
                            }
                            is RetrofitWrapper.Error -> {
                                emit(Resource.Error(textRes = R.string.weather_error))
                            }
                            else -> {}
                        }
                    }
                } else lastLoadedWeather?.let {
                    emit(Resource.Success(mapToNewCatchWeather(it, newCatchDate)))
                }
            }
        }

    private suspend fun mapToNewCatchWeather(
        data: WeatherForecast,
        catchDate: Long,
    ): NewCatchWeatherData {
        val index = getClosestHourIndex(list = data.hourly, date = catchDate)
        val weatherDescription = data.hourly[index].weather.first().description.replaceFirstChar { it.uppercase() }
        val weatherIcon = data.hourly[index].weather.first().icon
        val weatherTemperature = weatherSettings.getTemperatureUnit.first()
            .getTemperature(data.hourly[index].temperature)
        val weatherPressure = weatherSettings.getPressureUnit.first()
            .getPressure(data.hourly[index].pressure)
        val weatherWind = weatherSettings.getWindSpeedUnit.first()
            .getWindSpeedInt(data.hourly[index].windSpeed.toDouble())
        val weatherWindDeg = data.hourly[index].windDeg
        return NewCatchWeatherData(
            weatherDescription = weatherDescription,
            icon = weatherIcon,
            temperature = weatherTemperature,
            pressure = weatherPressure,
            wind = weatherWind,
            windDir = weatherWindDeg,
            moonPhase = calcMoonPhase(catchDate),
        )
    }

    private suspend fun getWeatherForecast(place: UserMapMarker) =
        weatherRepository.getWeather(place.latitude, place.longitude)

    private suspend fun getHistoricalWeather(place: UserMapMarker, newCatchDate: Long) =
        weatherRepository.getHistoricalWeather(
            place.latitude,
            place.longitude,
            (newCatchDate / 1000)
        )

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
package com.mobileprism.fishing.domain.use_cases

import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.weather.NewCatchWeatherData
import com.mobileprism.fishing.model.entity.weather.WeatherForecast
import com.mobileprism.fishing.model.repository.app.WeatherRepository
import com.mobileprism.fishing.utils.calcMoonPhase
import com.mobileprism.fishing.utils.getClosestHourIndex
import com.mobileprism.fishing.utils.isDateInList
import com.mobileprism.fishing.utils.isLocationsTooFar
import com.mobileprism.fishing.utils.time.hoursCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import java.util.*

class GetNewCatchWeatherUseCase(
    private val weatherRepository: WeatherRepository,
    private val weatherPreferences: WeatherPreferences
) {
    private var lastLoadedWeather: WeatherForecast? = null

    suspend operator fun invoke(place: UserMapMarker?, newCatchDate: Long) =
        flow<Result<NewCatchWeatherData>> {
            if (place == null) {
                emit(Result.failure(Throwable()))
            } else {
                if (checkWeatherDownloadNeed(place, newCatchDate)) {
                    downloadWeather(place, newCatchDate).single().fold(
                        onSuccess = {
                            lastLoadedWeather = it
                            emit(Result.success(createCatchWeatherData(place, it, newCatchDate)))
                        },
                        onFailure = {
                            emit(Result.failure(it))
                        }
                    )
                } else {
                    lastLoadedWeather?.let {
                        emit(Result.success(createCatchWeatherData(place, it, newCatchDate)))
                    }
                }
            }
        }

    private suspend fun downloadWeather(
        place: UserMapMarker,
        newCatchDate: Long
    ): Flow<Result<WeatherForecast>> {
        return if (Date().time.hoursCount() > newCatchDate.hoursCount()) {
            getHistoricalWeather(place, newCatchDate)
        } else {
            getWeatherForecast(place)
        }
    }

    private suspend fun createCatchWeatherData(
        location: UserMapMarker,
        weatherForecast: WeatherForecast,
        date: Long
    ): NewCatchWeatherData {
        val hour = getClosestHourIndex(weatherForecast.hourly, date)
        val tempUnits = weatherPreferences.getTemperatureUnit.first()
        val pressureUnits = weatherPreferences.getPressureUnit.first()
        val windUnits = weatherPreferences.getWindSpeedUnit.first()

        return NewCatchWeatherData(
            lat = location.latitude,
            lng = location.longitude,
            primary = weatherForecast.hourly[hour].weather.first().description.replaceFirstChar { it.uppercase() },
            icon = weatherForecast.hourly[hour].weather.first().icon,
            temperature = tempUnits.getTemperature(weatherForecast.hourly[hour].temperature),
            windSpeed = windUnits.getWindSpeed(weatherForecast.hourly[hour].windSpeed.toDouble()),
            windDeg = weatherForecast.hourly[hour].windDeg,
            pressure = pressureUnits.getPressure(weatherForecast.hourly[hour].pressure),
            moonPhase = calcMoonPhase(Date().time)
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
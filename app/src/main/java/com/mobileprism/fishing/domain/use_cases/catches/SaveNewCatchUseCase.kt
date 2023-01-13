package com.mobileprism.fishing.domain.use_cases.catches

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.raw.NewCatchWeather
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.domain.use_cases.SavePhotosUseCase
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.ui.viewmodels.*
import com.mobileprism.fishing.utils.getCurrentUserId
import com.mobileprism.fishing.utils.getNewCatchId
import com.mobileprism.fishing.utils.toStandardNumber
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take

class SaveNewCatchUseCase(
    private val catchesRepository: CatchesRepository,
    private val savePhotos: SavePhotosUseCase,
    private val weatherPreferences: WeatherPreferences
) {

    operator fun invoke(data: NewUserCatchData) = channelFlow {

        val userCatch = createUserCatch(
            placeAndTimeState = data.placeAndTimeState,
            fishAndWeightState = data.fishAndWeightState,
            catchInfoState = data.catchInfoState,
            weather = mapWeatherValues(data.catchWeatherState),
            photos = savePhotos(data.photos),
        )

        data.placeAndTimeState.place?.let { it ->
            catchesRepository.addNewCatch(markerId = it.id, newCatch = userCatch)
                .collect { trySend(it) }
        }
    }

    private suspend fun mapWeatherValues(weatherState: CatchWeatherState): NewCatchWeather {
        val tempUnits = weatherPreferences.getTemperatureUnit.take(1).first()
        val pressureUnits = weatherPreferences.getPressureUnit.take(1).first()
        val windUnits = weatherPreferences.getWindSpeedUnit.take(1).first()

        return NewCatchWeather(
            fishingWeather = weatherState.weather,
            temperatureInC = tempUnits.getDefaultTemperature(
                weatherState.temperature.toStandardNumber().toDouble()
            ),
            pressureInMmhg = pressureUnits.getPressureMmhg(
                weatherState.pressure.toStandardNumber().toDouble()
            ),
            windInMs = windUnits.getDefaultWindSpeed(
                weatherState.windSpeed.toStandardNumber().toDouble()
            ).toInt(),
            windDirInDeg = weatherState.windDeg.toFloat(),
            moonPhase = weatherState.moonPhase
        )
    }

    private fun createUserCatch(
        placeAndTimeState: CatchPlaceAndTimeState,
        fishAndWeightState: FishAndWeightState,
        catchInfoState: CatchInfoState,
        weather: NewCatchWeather,
        photos: List<String>
    ) = UserCatch(
        id = getNewCatchId(),
        markerId = placeAndTimeState.place?.id ?: "",
        userId = getCurrentUserId(),
        description = catchInfoState.note,
        date = placeAndTimeState.date,
        fishType = fishAndWeightState.fish,
        fishAmount = fishAndWeightState.fishAmount,
        fishWeight = fishAndWeightState.fishWeight,
        fishingRodType = catchInfoState.rod,
        fishingBait = catchInfoState.bait,
        fishingLure = catchInfoState.lure,
        isPublic = false,
        downloadPhotoLinks = photos,
        placeTitle = placeAndTimeState.place?.title ?: "",
        weather = weather.fishingWeather,
        weatherTemperature = weather.temperatureInC.toFloat(),
        weatherWindSpeed = weather.windInMs.toFloat(),
        weatherWindDeg = weather.windDirInDeg.toInt(),
        weatherPressure = weather.pressureInMmhg,
        weatherMoonPhase = weather.moonPhase
    )

}
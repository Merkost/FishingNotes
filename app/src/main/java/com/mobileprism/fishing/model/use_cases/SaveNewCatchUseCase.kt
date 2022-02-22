package com.mobileprism.fishing.model.use_cases

import android.net.Uri
import com.mobileprism.fishing.domain.*
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.model.entity.common.Progress
import com.mobileprism.fishing.model.entity.content.UserCatch
import com.mobileprism.fishing.model.entity.raw.NewCatchWeather
import com.mobileprism.fishing.model.repository.PhotoStorage
import com.mobileprism.fishing.model.repository.app.CatchesRepository
import com.mobileprism.fishing.utils.getCurrentUser
import com.mobileprism.fishing.utils.getNewCatchId
import com.mobileprism.fishing.utils.network.ConnectionManager
import com.mobileprism.fishing.utils.network.ConnectionState
import kotlinx.coroutines.flow.channelFlow

class SaveNewCatchUseCase(
    private val catchesRepository: CatchesRepository,
    private val photosRepository: PhotoStorage,
    private val connectionManager: ConnectionManager,
    private val weatherPreferences: WeatherPreferences
) {

    operator fun invoke(data: NewUserCatchData) = channelFlow<Progress> {
        trySend(Progress.Loading())

        val userCatch = createUserCatch(
            placeAndTime = data.placeAndTime,
            fishAndWeight = data.fishAndWeight,
            catchInfo = data.catchInfo,
            weather = mapWeatherValues(data.catchWeather),
            photos = savePhotos(data.photos)
        )

        data.placeAndTime.place?.let { it ->
            if (connectionManager.getConnectionState() is ConnectionState.Available) {
                catchesRepository.addNewCatch(it.id, userCatch).collect { progerss ->
                    trySend(progerss)
                }
            } else {
                catchesRepository.addNewCatchOffline(it.id, userCatch).collect { progerss ->
                    trySend(progerss)
                }
            }
        }
    }

    private suspend fun savePhotos(photos: List<Uri>): List<String> {
        return photosRepository.uploadPhotos(photos)
    }

    private suspend fun mapWeatherValues(weather: CatchWeather): NewCatchWeather {
        val tempUnits = weatherPreferences.getTemperatureUnit()
        val pressureUnits = weatherPreferences.getPressureUnit()
        val windUnits = weatherPreferences.getWindSpeedUnit()

        return NewCatchWeather(
            weatherDescription = weather.primary.replaceFirstChar { it.uppercase() },
            icon = weather.icon,
            temperatureInC = tempUnits.getCelciusTemperature(weather.temperature.toFloat()),
            pressureInMmhg = pressureUnits.getDefaultPressure(weather.pressure.toFloat()),
            windInMs = windUnits.getWindSpeedInt(weather.windSpeed.toDouble()).toInt(),
            windDirInDeg = weather.windDeg.toFloat(),
            moonPhase = weather.moonPhase
        )
    }

    private fun createUserCatch(
        placeAndTime: CatchPlaceAndTime,
        fishAndWeight: FishAndWeight,
        catchInfo: CatchInfo,
        weather: NewCatchWeather,
        photos: List<String>
    ) = UserCatch(
        id = getNewCatchId(),
        userId = getCurrentUser()!!.uid,
        description = catchInfo.note,
        date = placeAndTime.date,
        fishType = fishAndWeight.fish,
        fishAmount = fishAndWeight.fishAmount,
        fishWeight = fishAndWeight.fishWeight,
        fishingRodType = catchInfo.rod,
        fishingBait = catchInfo.bait,
        fishingLure = catchInfo.lure,
        userMarkerId = placeAndTime.place?.id ?: "",
        isPublic = false,
        downloadPhotoLinks = photos,
        placeTitle = placeAndTime.place?.title ?: "",
        weatherPrimary = weather.weatherDescription,
        weatherIcon = weather.icon,
        weatherTemperature = weather.temperatureInC.toFloat(),
        weatherWindSpeed = weather.windInMs.toFloat(),
        weatherWindDeg = weather.windDirInDeg.toInt(),
        weatherPressure = weather.pressureInMmhg,
        weatherMoonPhase = weather.moonPhase
    )

}
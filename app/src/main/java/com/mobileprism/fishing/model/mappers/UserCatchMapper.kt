package com.mobileprism.fishing.model.mappers

import com.mobileprism.fishing.model.entity.content.UserCatch
import com.mobileprism.fishing.model.entity.raw.RawUserCatch
import com.mobileprism.fishing.utils.getCurrentUser
import com.mobileprism.fishing.utils.getNewCatchId

class UserCatchMapper {

    fun mapRawCatch(newCatch: RawUserCatch, photoLinks: List<String>? = null) = UserCatch(
        id = getNewCatchId(),
        userId = getCurrentUser()!!.uid,
        description = newCatch.description ?: "",
        date = newCatch.date,
        fishType = newCatch.fishType,
        fishAmount = newCatch.fishAmount ?: 0,
        fishWeight = newCatch.fishWeight ?: 0.0,
        fishingRodType = newCatch.fishingRodType ?: "",
        fishingBait = newCatch.fishingBait ?: "",
        fishingLure = newCatch.fishingLure ?: "",
        userMarkerId = newCatch.markerId,
        isPublic = newCatch.isPublic,
        downloadPhotoLinks = photoLinks ?: listOf(),
        placeTitle = newCatch.placeTitle,
        weatherPrimary = newCatch.weatherPrimary,
        weatherIcon = newCatch.weatherIcon,
        weatherTemperature = newCatch.weatherTemperature,
        weatherWindSpeed = newCatch.weatherWindSpeed,
        weatherWindDeg = newCatch.weatherWindDeg,
        weatherPressure = newCatch.weatherPressure,
        weatherMoonPhase = newCatch.weatherMoonPhase
    )
}
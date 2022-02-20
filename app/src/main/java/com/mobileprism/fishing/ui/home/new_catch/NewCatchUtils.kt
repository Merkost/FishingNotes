package com.mobileprism.fishing.ui.home.new_catch

import android.net.Uri
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.raw.RawUserCatch

sealed class ReceivedPlaceState() {
    object NotReceived : ReceivedPlaceState()
    class Received(val place: UserMapMarker) : ReceivedPlaceState()
}

sealed class NewCatchPlacesState() {
    object NotReceived : NewCatchPlacesState()
    class Received(val locations: List<UserMapMarker>) : NewCatchPlacesState()
}

sealed class DropdownMenuState {
    object Closed : DropdownMenuState()
    object Opened : DropdownMenuState()
}

fun isThatPlaceInList(
    textFieldValue: String,
    suggestions: List<UserMapMarker>
): Boolean {
    suggestions.forEach {
        if (it.title == textFieldValue) return true
    }
    return false
}

fun searchFor(
    what: String,
    where: List<UserMapMarker>,
    filteredList: MutableList<UserMapMarker>
) {
    filteredList.clear()
    where.forEach {
        if (it.title.contains(what, ignoreCase = true)) {
            filteredList.add(it)
        }
    }
}

interface NewCatchBuilder {
    fun setPlaceId(id: String)
    fun setPlaceTitle(title: String)
    fun setDate(date: Long)
    fun setDescription(description: String)
    fun setFishType(fish: String)
    fun setFishWeight(weight: Double)
    fun setFishAmount(amount: Int)
    fun setRodType(rod: String)
    fun setBait(bait: String)
    fun setLure(lure: String)
    fun setPhotos(photos: List<Uri>)
    fun setWeatherPrimary(weather: String)
    fun setWeatherIcon(icon: String)
    fun setWeatherTemperature(temperature: Float)
    fun setWeatherWindSpeed(windSpeed: Float)
    fun setWeatherWindDegrees(windDeg: Int)
    fun setWeatherPressure(pressure: Int)
    fun setWeatherMoonPhase(moonPhase: Float)
    fun create(): RawUserCatch
}

class NewCatchBuilderImpl() : NewCatchBuilder {
    val result = RawUserCatch()

    override fun setPlaceId(id: String) {
        result.markerId = id
    }

    override fun setPlaceTitle(title: String) {
        result.placeTitle = title
    }

    override fun setDate(date: Long) {
        result.date = date
    }

    override fun setDescription(description: String) {
        result.description = description
    }

    override fun setFishType(fish: String) {
        result.fishType = fish
    }

    override fun setFishWeight(weight: Double) {
        result.fishWeight = weight
    }

    override fun setFishAmount(amount: Int) {
        result.fishAmount = amount
    }

    override fun setRodType(rod: String) {
        result.fishingRodType = rod
    }

    override fun setBait(bait: String) {
        result.fishingBait = bait
    }

    override fun setLure(lure: String) {
        result.fishingLure = lure
    }

    override fun setPhotos(photos: List<Uri>) {
        result.photos = photos
    }

    override fun setWeatherPrimary(weather: String) {
        result.weatherPrimary = weather
    }

    override fun setWeatherIcon(icon: String) {
        result.weatherIcon = icon
    }

    override fun setWeatherTemperature(temperature: Float) {
        result.weatherTemperature = temperature
    }

    override fun setWeatherWindSpeed(windSpeed: Float) {
        result.weatherWindSpeed = windSpeed
    }

    override fun setWeatherWindDegrees(windDeg: Int) {
        result.weatherWindDeg = windDeg
    }

    override fun setWeatherPressure(pressure: Int) {
        result.weatherPressure = pressure
    }

    override fun setWeatherMoonPhase(moonPhase: Float) {
        result.weatherMoonPhase = moonPhase
    }

    override fun create(): RawUserCatch {
        return result
    }
}
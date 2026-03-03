package com.mobileprism.fishing.testutils

import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.raw.RawMapMarker
import com.mobileprism.fishing.domain.entity.weather.Hourly
import com.mobileprism.fishing.domain.entity.weather.Weather
import com.mobileprism.fishing.domain.entity.weather.WeatherForecast
import com.mobileprism.fishing.model.datasource.local.entity.CatchEntity
import com.mobileprism.fishing.model.datasource.local.entity.MarkerEntity
import com.mobileprism.fishing.model.datasource.local.entity.SyncStatus

fun userCatch(
    id: String = "catch-1",
    userId: String = "user-1",
    date: Long = 1700000000000L,
    fishType: String = "Bass",
    fishWeight: Double = 2.5,
    fishAmount: Int = 1,
    userMarkerId: String = "marker-1",
    placeTitle: String = "Lake",
    downloadPhotoLinks: List<String> = emptyList(),
    weatherPrimary: String = "Clear",
    weatherTemperature: Float = 20.0f,
    weatherMoonPhase: Float = 0.5f,
    weatherWindSpeed: Float = 3.0f,
    weatherWindDeg: Int = 180,
    weatherPressure: Int = 1013,
    note: Note = Note(),
    fishingRodType: String = "",
    fishingBait: String = "",
    fishingLure: String = "",
    lastModified: Long = 0,
) = UserCatch(
    id = id,
    userId = userId,
    date = date,
    fishType = fishType,
    fishWeight = fishWeight,
    fishAmount = fishAmount,
    userMarkerId = userMarkerId,
    placeTitle = placeTitle,
    downloadPhotoLinks = downloadPhotoLinks,
    weatherPrimary = weatherPrimary,
    weatherTemperature = weatherTemperature,
    weatherMoonPhase = weatherMoonPhase,
    weatherWindSpeed = weatherWindSpeed,
    weatherWindDeg = weatherWindDeg,
    weatherPressure = weatherPressure,
    note = note,
    fishingRodType = fishingRodType,
    fishingBait = fishingBait,
    fishingLure = fishingLure,
    lastModified = lastModified,
)

fun userMapMarker(
    id: String = "marker-1",
    userId: String = "user-1",
    latitude: Double = 55.0,
    longitude: Double = 37.0,
    title: String = "My Place",
    catchesCount: Int = 0,
    notes: List<Note> = emptyList(),
    description: String = "",
    markerColor: Int = UserMapMarker.DEFAULT_MARKER_COLOR,
    dateOfCreation: Long = 0,
    visible: Boolean = true,
    public: Boolean = false,
    lastModified: Long = 0,
) = UserMapMarker(
    id = id,
    userId = userId,
    latitude = latitude,
    longitude = longitude,
    title = title,
    catchesCount = catchesCount,
    notes = notes,
    description = description,
    markerColor = markerColor,
    dateOfCreation = dateOfCreation,
    visible = visible,
    public = public,
    lastModified = lastModified,
)

fun note(
    id: String = "note-1",
    title: String = "Test Note",
    description: String = "Test description",
    dateCreated: Long = 1700000000000L,
) = Note(
    id = id,
    title = title,
    description = description,
    dateCreated = dateCreated,
)

fun user(
    uid: String = "user-1",
    displayName: String = "John",
    email: String = "john@test.com",
    login: String = "johnd",
    birthDate: Long = 0,
    registerDate: Long = 0,
) = User(
    uid = uid,
    displayName = displayName,
    email = email,
    login = login,
    birthDate = birthDate,
    registerDate = registerDate,
)

fun rawMapMarker(
    title: String = "New Place",
    latitude: Double = 55.0,
    longitude: Double = 37.0,
    markerColor: Int = UserMapMarker.DEFAULT_MARKER_COLOR,
    description: String = "",
    visible: Boolean = true,
    public: Boolean = false,
) = RawMapMarker(
    title = title,
    latitude = latitude,
    longitude = longitude,
    markerColor = markerColor,
    description = description,
    visible = visible,
    public = public,
)

fun catchEntity(
    id: String = "catch-1",
    userId: String = "user-1",
    downloadPhotoLinks: String = """["http://example.com/photo1.jpg"]""",
    fishType: String = "Bass",
    fishWeight: Double = 2.5,
    fishAmount: Int = 1,
    date: Long = 1700000000000L,
    userMarkerId: String = "marker-1",
    placeTitle: String = "Lake",
    syncStatus: Int = SyncStatus.SYNCED,
    noteId: String = "",
    noteTitle: String = "",
    noteDescription: String = "",
    noteDateCreated: Long = 0,
    weatherPrimary: String = "Clear",
    weatherTemperature: Float = 20.0f,
    weatherMoonPhase: Float = 0.5f,
    lastModified: Long = 1700000000000L,
) = CatchEntity(
    id = id,
    userId = userId,
    downloadPhotoLinks = downloadPhotoLinks,
    fishType = fishType,
    fishWeight = fishWeight,
    fishAmount = fishAmount,
    date = date,
    userMarkerId = userMarkerId,
    placeTitle = placeTitle,
    syncStatus = syncStatus,
    noteId = noteId,
    noteTitle = noteTitle,
    noteDescription = noteDescription,
    noteDateCreated = noteDateCreated,
    weatherPrimary = weatherPrimary,
    weatherTemperature = weatherTemperature,
    weatherMoonPhase = weatherMoonPhase,
    lastModified = lastModified,
)

fun markerEntity(
    id: String = "marker-1",
    userId: String = "user-1",
    notes: String = """[{"id":"n1","title":"Note","description":"Desc","dateCreated":0}]""",
    latitude: Double = 55.0,
    longitude: Double = 37.0,
    title: String = "My Place",
    markerColor: Int = 0xFF0000,
    catchesCount: Int = 3,
    syncStatus: Int = SyncStatus.SYNCED,
    lastModified: Long = 1700000000000L,
) = MarkerEntity(
    id = id,
    userId = userId,
    notes = notes,
    latitude = latitude,
    longitude = longitude,
    title = title,
    markerColor = markerColor,
    catchesCount = catchesCount,
    syncStatus = syncStatus,
    lastModified = lastModified,
)

fun weatherForecast(
    latitude: Double = 55.0,
    longitude: Double = 37.0,
    hourlyCount: Int = 6,
) = WeatherForecast(
    latitude = latitude,
    longitude = longitude,
    hourly = (0 until hourlyCount).map { i ->
        Hourly(
            date = 1700000000L + i * 3600L,
            temperature = (15 + i).toFloat(),
            pressure = 1013,
            windSpeed = 3.0f,
            windDeg = 180,
            weather = listOf(Weather(description = "clear sky", icon = "01d")),
        )
    },
)

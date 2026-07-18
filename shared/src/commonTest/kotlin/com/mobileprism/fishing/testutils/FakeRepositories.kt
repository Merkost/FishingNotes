package com.mobileprism.fishing.testutils

import com.mobileprism.fishing.domain.entity.common.ContentState
import com.mobileprism.fishing.domain.entity.common.ContentStateOld
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.MapMarker
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.solunar.Solunar
import com.mobileprism.fishing.domain.entity.weather.CurrentWeatherFree
import com.mobileprism.fishing.domain.entity.weather.WeatherForecast
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.domain.repository.PhotoStorage
import com.mobileprism.fishing.domain.repository.app.FreeWeatherRepository
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.domain.repository.app.SolunarRepository
import com.mobileprism.fishing.domain.repository.app.WeatherRepository
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryRead
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeCatchesRepositoryRead(
    private val catches: List<UserCatch> = emptyList(),
    private val catchesState: MutableSharedFlow<ContentStateOld<UserCatch>> = MutableSharedFlow(),
) : CatchesRepositoryRead {
    override fun getAllUserCatchesList(): Flow<List<UserCatch>> = flowOf(catches)
    override fun getAllUserCatchesState(): Flow<ContentStateOld<UserCatch>> = catchesState
    override fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>> =
        flowOf(catches.filter { it.userMarkerId == markerId })
}

class FakeAuthRepository(
    private val userId: String = "user-1",
) : AuthRepository {
    override fun getCurrentUserId(): String = userId
    override fun getCurrentUserIdOrNull(): String? = userId
    override val currentUserIdFlow: Flow<String?> = flowOf(userId)
}

class FakeMarkersRepository(
    private val markers: Map<String, Result<UserMapMarker>> = emptyMap()
) : MarkersRepository {
    var saveNewNoteResult: Result<Unit> = Result.success(Unit)
    var updateNotesResult: Result<Unit> = Result.success(Unit)
    var changeVisibilityResult: Result<Unit> = Result.success(Unit)
    var deleteMarkerResult: Result<Unit> = Result.success(Unit)
    var addNewMarkerResult: Result<Unit> = Result.success(Unit)

    override suspend fun getMapMarker(markerId: String): Result<UserMapMarker> {
        return markers[markerId] ?: Result.failure(RuntimeException("Not found"))
    }
    override fun getAllUserMarkers(): Flow<ContentState<MapMarker>> = error("Not used")
    override fun getAllUserMarkersList(): Flow<List<UserMapMarker>> = flowOf(markers.values.mapNotNull { it.getOrNull() })
    override suspend fun saveNewNote(markerId: String, newNote: Note): Result<Unit> = saveNewNoteResult
    override suspend fun updateNotes(markerId: String, notes: List<Note>): Result<Unit> = updateNotesResult
    override suspend fun changeMarkerVisibility(marker: UserMapMarker, changeTo: Boolean): Result<Unit> = changeVisibilityResult
    override suspend fun deleteMarker(userMapMarker: UserMapMarker): Result<Unit> = deleteMarkerResult
    override suspend fun addNewMarker(newMarker: UserMapMarker): Result<Unit> = addNewMarkerResult
}

class FakeWeatherRepository : WeatherRepository {
    var getWeatherResults = mutableListOf<Result<WeatherForecast>>()
    var getHistoricalWeatherResults = mutableListOf<Result<WeatherForecast>>()
    private var weatherCallIndex = 0
    private var historicalCallIndex = 0

    val getWeatherCalls = mutableListOf<Pair<Double, Double>>()
    val getHistoricalWeatherCalls = mutableListOf<Triple<Double, Double, Long>>()

    override suspend fun getWeather(lat: Double, lon: Double): Result<WeatherForecast> {
        getWeatherCalls.add(lat to lon)
        val idx = (weatherCallIndex++).coerceAtMost(getWeatherResults.lastIndex)
        return getWeatherResults[idx]
    }

    override suspend fun getHistoricalWeather(lat: Double, lon: Double, date: Long): Result<WeatherForecast> {
        getHistoricalWeatherCalls.add(Triple(lat, lon, date))
        val idx = (historicalCallIndex++).coerceAtMost(getHistoricalWeatherResults.lastIndex)
        return getHistoricalWeatherResults[idx]
    }
}

class FakeFreeWeatherRepository : FreeWeatherRepository {
    var result: Result<CurrentWeatherFree> = Result.failure(NotImplementedError())

    override suspend fun getCurrentWeatherFree(lat: Double, lon: Double): Result<CurrentWeatherFree> = result
}

class FakeSolunarRepository : SolunarRepository {
    var result: Result<Solunar> = Result.failure(NotImplementedError())
    val calls = mutableListOf<List<Any>>()

    override suspend fun getSolunar(latitude: Double, longitude: Double, date: String, timeZone: Int): Result<Solunar> {
        calls.add(listOf(latitude, longitude, date, timeZone))
        return result
    }
}

class FakePhotoStorage : PhotoStorage {
    val uploadedPhotos = mutableListOf<String>()
    val deletedPhotos = mutableListOf<String>()
    var uploadResult: List<String> = emptyList()

    override suspend fun uploadPhotos(
        photos: List<String>,
        onProgress: ((uploaded: Int, total: Int) -> Unit)?
    ): Result<List<String>> {
        uploadedPhotos.addAll(photos)
        val result = if (uploadResult.isNotEmpty()) uploadResult else photos.map { "http://uploaded/$it" }
        return Result.success(result)
    }

    override suspend fun deletePhoto(url: String): Result<Unit> {
        deletedPhotos.add(url)
        return Result.success(Unit)
    }
}

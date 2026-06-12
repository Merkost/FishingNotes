package com.mobileprism.fishing.viewmodels

import com.mobileprism.fishing.domain.entity.common.ContentState
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.MapMarker
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.solunar.Solunar
import com.mobileprism.fishing.domain.entity.weather.CurrentWeatherFree
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.domain.use_cases.GetFishActivityUseCase
import com.mobileprism.fishing.domain.use_cases.GetFreeWeatherUseCase
import com.mobileprism.fishing.domain.use_cases.PlaceNameResolver
import com.mobileprism.fishing.domain.use_cases.places.AddNewPlaceUseCase
import com.mobileprism.fishing.domain.use_cases.places.GetUserPlacesListUseCase
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.testutils.FakeAuthRepository
import com.mobileprism.fishing.testutils.FakeFreeWeatherRepository
import com.mobileprism.fishing.testutils.FakeSolunarRepository
import com.mobileprism.fishing.ui.home.map.GeocoderResult
import com.mobileprism.fishing.ui.home.map.LocationState
import com.mobileprism.fishing.ui.home.map.MapCameraState
import com.mobileprism.fishing.ui.utils.enums.AppThemeValues
import com.mobileprism.fishing.ui.utils.enums.DarkModeValues
import com.mobileprism.fishing.utils.location.LocationManager
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `marker stats failures do not crash or keep loading forever`() = runTest {
        val viewModel = mapViewModel(
            freeWeatherResult = Result.failure(RuntimeException("Weather timeout")),
            solunarResult = Result.failure(RuntimeException("Solunar timeout")),
        )

        viewModel.setNewMarkerInfo(latitude = 55.0, longitude = 37.0)
        advanceUntilIdle()

        assertFalse(viewModel.placeStatsLoading.value)
        assertNull(viewModel.currentWeather.value)
        assertNull(viewModel.fishActivity.value)
    }

    private fun mapViewModel(
        freeWeatherResult: Result<CurrentWeatherFree>,
        solunarResult: Result<Solunar>,
    ): MapViewModel {
        val markersRepository = fakeMarkersRepository()
        val freeWeatherRepository = FakeFreeWeatherRepository().apply {
            result = freeWeatherResult
        }
        val solunarRepository = FakeSolunarRepository().apply {
            result = solunarResult
        }

        return MapViewModel(
            getUserPlacesListUseCase = GetUserPlacesListUseCase(markersRepository),
            addNewPlaceUseCase = AddNewPlaceUseCase(markersRepository, FakeAuthRepository()),
            getFreeWeatherUseCase = GetFreeWeatherUseCase(freeWeatherRepository),
            getFishActivityUseCase = GetFishActivityUseCase(solunarRepository),
            getPlaceNameUseCase = fakePlaceNameResolver(),
            userPreferences = fakeUserPreferences(),
            locationManager = fakeLocationManager(),
        )
    }

    private fun fakeMarkersRepository() = object : MarkersRepository {
        override suspend fun getMapMarker(markerId: String): Result<UserMapMarker> =
            Result.failure(IllegalArgumentException("Not used"))

        override fun getAllUserMarkers(): Flow<ContentState<MapMarker>> = error("Not used")

        override fun getAllUserMarkersList(): Flow<List<UserMapMarker>> = flowOf(mutableListOf())

        override suspend fun saveNewNote(markerId: String, newNote: Note): Result<Unit> =
            Result.success(Unit)

        override suspend fun updateNotes(markerId: String, notes: List<Note>): Result<Unit> =
            Result.success(Unit)

        override suspend fun changeMarkerVisibility(
            marker: UserMapMarker,
            changeTo: Boolean
        ): Result<Unit> = Result.success(Unit)

        override suspend fun deleteMarker(userMapMarker: UserMapMarker): Result<Unit> =
            Result.success(Unit)

        override suspend fun addNewMarker(newMarker: UserMapMarker): Result<Unit> =
            Result.success(Unit)
    }

    private fun fakePlaceNameResolver() = object : PlaceNameResolver {
        override suspend fun invoke(latitude: Double, longitude: Double): Flow<GeocoderResult> =
            flowOf(GeocoderResult.NoNamePlace)
    }

    private fun fakeLocationManager() = object : LocationManager {
        override fun getCurrentLocationFlow(): Flow<LocationState> =
            flowOf(LocationState.NoPermission)
    }

    private fun fakeUserPreferences() = object : UserPreferences {
        override val shouldShowLocationPermission: Flow<Boolean> = flowOf(true)
        override val use12hTimeFormat: Flow<Boolean> = flowOf(false)
        override val appTheme: Flow<AppThemeValues> = flowOf(AppThemeValues.Blue)
        override val darkMode: Flow<DarkModeValues> = flowOf(DarkModeValues.System)
        override val useFabFastAdd: Flow<Boolean> = flowOf(false)
        override val useMapZoomButons: Flow<Boolean> = flowOf(false)
        override val shouldShowHiddenPlacesOnMap: Flow<Boolean> = flowOf(true)
        override val getLastMapCameraLocation: Flow<MapCameraState> = flowOf(MapCameraState())
        override val hasCompletedOnboarding: Flow<Boolean> = flowOf(true)
        override val hasPromptCardDismissed: Flow<Boolean> = flowOf(false)

        override suspend fun saveLocationPermissionStatus(shouldShow: Boolean) {}
        override suspend fun saveTimeFormatStatus(use12hFormat: Boolean) {}
        override suspend fun saveAppTheme(appTheme: AppThemeValues) {}
        override suspend fun saveDarkMode(darkMode: DarkModeValues) {}
        override suspend fun saveFabFastAdd(fastAdd: Boolean) {}
        override suspend fun saveMapZoomButtons(useZoomButtons: Boolean) {}
        override suspend fun saveMapHiddenPlaces(shouldShow: Boolean) {}
        override suspend fun saveLastMapCameraLocation(cameraState: MapCameraState) {}
        override suspend fun saveOnboardingCompleted(completed: Boolean) {}
        override suspend fun savePromptCardDismissed(dismissed: Boolean) {}
    }
}

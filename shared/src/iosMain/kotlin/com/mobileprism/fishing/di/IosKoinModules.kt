package com.mobileprism.fishing.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.mobileprism.fishing.domain.repository.SyncStatusProvider
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.domain.use_cases.IosGeocoder
import com.mobileprism.fishing.domain.use_cases.PlatformGeocoder
import com.mobileprism.fishing.domain.use_cases.PlaceNameResolver
import com.mobileprism.fishing.model.datasource.firebase.IosAnalyticsTracker
import com.mobileprism.fishing.model.datasource.local.sync.SyncStatusManager
import com.mobileprism.fishing.model.datastore.NotesPreferences
import com.mobileprism.fishing.model.datastore.UserDatastore
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.datastore.UserPreferencesImpl
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.model.datastore.impl.NotesPreferencesImpl
import com.mobileprism.fishing.model.datastore.impl.UserDatastoreImpl
import com.mobileprism.fishing.model.datastore.impl.WeatherPreferencesImpl
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.viewmodels.LoginViewModel
import com.mobileprism.fishing.ui.viewmodels.NewCatchMasterViewModel
import com.mobileprism.fishing.ui.viewmodels.UserCatchViewModel
import com.mobileprism.fishing.ui.viewmodels.UserCatchesViewModel
import com.mobileprism.fishing.ui.viewmodels.UserPlaceViewModel
import com.mobileprism.fishing.ui.viewmodels.UserPlacesViewModel
import com.mobileprism.fishing.ui.viewmodels.WeatherViewModel
import com.mobileprism.fishing.utils.location.IosLocationManager
import com.mobileprism.fishing.utils.location.LocationManager
import com.mobileprism.fishing.utils.network.ConnectionManager
import com.mobileprism.fishing.utils.network.IosConnectionManager
import com.mobileprism.fishing.viewmodels.MapViewModel
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

val appModule = module {
    single<PlatformGeocoder> { IosGeocoder() }
    single { SnackbarManager }
    single<AnalyticsTracker> { IosAnalyticsTracker() }
    single<LocationManager> { IosLocationManager() }
    single<SyncStatusProvider> { get<SyncStatusManager>() }
}

val settingsModule = module {
    single<UserDatastore> { UserDatastoreImpl(createIosPreferencesDataStore("appSettings")) }
    single { UserPreferencesImpl(createIosPreferencesDataStore("userSettings")) }
    single<UserPreferences> { get<UserPreferencesImpl>() }
    single<WeatherPreferences> { WeatherPreferencesImpl(createIosPreferencesDataStore("weatherSettings")) }
    single<NotesPreferences> { NotesPreferencesImpl(createIosPreferencesDataStore("notesSettings")) }
    single<ConnectionManager> { IosConnectionManager() }
}

val mainModule = module {
    viewModel {
        LoginViewModel(
            repository = get(),
            analyticsTracker = get()
        )
    }
    viewModel {
        MapViewModel(
            getUserPlacesListUseCase = get(),
            addNewPlaceUseCase = get(),
            getFreeWeatherUseCase = get(),
            getFishActivityUseCase = get(),
            getPlaceNameUseCase = get<PlaceNameResolver>(),
            userPreferences = get(),
            locationManager = get(),
        )
    }
    viewModel { parameters ->
        UserCatchViewModel(
            userCatch = parameters.get(),
            updateUserCatch = get(),
            deleteUserCatch = get(),
            getMapMarkerById = get(),
            subscribeOnUserCatchState = get()
        )
    }
    viewModel {
        WeatherViewModel(
            weatherRepository = get(),
            repository = get()
        )
    }
    viewModel {
        UserPlaceViewModel(
            markersRepo = get(),
            catchesRepo = get<com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryRead>(),
            saveNewUserMarkerNoteUseCase = get(),
            deleteUserMarkerNoteUseCase = get()
        )
    }
    viewModel { UserCatchesViewModel(userCatchesUseCase = get(), repository = get()) }
    viewModel { UserPlacesViewModel(repository = get()) }
    viewModel { parameters ->
        NewCatchMasterViewModel(
            placeState = parameters.get(),
            getNewCatchWeatherUseCase = get(),
            saveNewCatchUseCase = get(),
            getUserPlacesListUseCase = get()
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun createIosPreferencesDataStore(name: String) =
    PreferenceDataStoreFactory.createWithPath {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )!!
        "${documentDirectory.path}/datastore/$name.preferences_pb".toPath()
    }

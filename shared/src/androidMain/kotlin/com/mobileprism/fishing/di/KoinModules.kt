package com.mobileprism.fishing.di

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.mobileprism.fishing.domain.repository.SyncStatusProvider
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.model.datasource.firebase.FirebaseAnalyticsTracker
import com.mobileprism.fishing.model.datasource.local.sync.SyncStatusManager
import com.mobileprism.fishing.model.datastore.*
import com.mobileprism.fishing.model.datastore.UserPreferencesImpl
import com.mobileprism.fishing.model.datastore.impl.NotesPreferencesImpl
import com.mobileprism.fishing.model.datastore.impl.UserDatastoreImpl
import com.mobileprism.fishing.model.datastore.impl.WeatherPreferencesImpl
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.viewmodels.*
import com.mobileprism.fishing.utils.location.LocationManager
import com.mobileprism.fishing.utils.location.LocationManagerImpl
import com.mobileprism.fishing.utils.network.ConnectionManager
import com.mobileprism.fishing.utils.network.ConnectionManagerImpl
import com.mobileprism.fishing.domain.use_cases.GetPlaceNameUseCase
import com.mobileprism.fishing.domain.use_cases.PlaceNameResolver
import com.mobileprism.fishing.viewmodels.MapViewModel
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<Geocoder> { createGeocoder(androidContext()) }
    single<AppUpdateManager> { AppUpdateManagerFactory.create(androidContext()) }
    single { SnackbarManager }
    single<AnalyticsTracker> { FirebaseAnalyticsTracker(Firebase.analytics) }
    single { LocationManagerImpl(get()) }
    single<LocationManager> { get<LocationManagerImpl>() }
    single<PlaceNameResolver> { get<GetPlaceNameUseCase>() }
    single<SyncStatusProvider> { get<SyncStatusManager>() }
}

val settingsModule = module {
    single<UserDatastore> { UserDatastoreImpl(createPreferencesDataStore(androidContext(), "appSettings")) }
    single { UserPreferencesImpl(createPreferencesDataStore(androidContext(), "userSettings")) }
    single<UserPreferences> { get<UserPreferencesImpl>() }
    single<WeatherPreferences> { WeatherPreferencesImpl(createPreferencesDataStore(androidContext(), "weatherSettings")) }
    single<NotesPreferences> { NotesPreferencesImpl(createPreferencesDataStore(androidContext(), "notesSettings")) }
    single<ConnectionManager> { ConnectionManagerImpl(androidContext()) }
}

val mainModule = module {
    viewModel { LoginViewModel(
        repository = get(),
        analyticsTracker = get()
    ) }
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


fun createPreferencesDataStore(context: Context, name: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath {
        context.filesDir.resolve("datastore/$name.preferences_pb").absolutePath.toPath()
    }

fun createGeocoder(androidContext: Context): Geocoder {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Geocoder(androidContext, androidContext.resources.configuration.locales[0])
    } else {
        Geocoder(androidContext)
    }
}

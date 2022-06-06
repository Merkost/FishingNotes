package com.mobileprism.fishing.di

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.android.billingclient.api.BillingClient
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.mobileprism.fishing.model.datastore.*
import com.mobileprism.fishing.model.datastore.impl.NotesPreferencesImpl
import com.mobileprism.fishing.model.datastore.impl.UserDatastoreImpl
import com.mobileprism.fishing.model.datastore.impl.WeatherPreferencesImpl
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.viewmodels.*
import com.mobileprism.fishing.utils.Logger
import com.mobileprism.fishing.utils.location.LocationManager
import com.mobileprism.fishing.utils.location.LocationManagerImpl
import com.mobileprism.fishing.utils.network.ConnectionManager
import com.mobileprism.fishing.utils.network.ConnectionManagerImpl
import com.mobileprism.fishing.viewmodels.EditProfileViewModel
import com.mobileprism.fishing.viewmodels.MainViewModel
import com.mobileprism.fishing.viewmodels.MapViewModel
import com.mobileprism.fishing.ui.viewmodels.MainViewModel
import com.mobileprism.fishing.ui.viewmodels.MapViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { Logger() }
    single<Geocoder> { createGeocoder(androidContext()) }
    single<AppUpdateManager> { AppUpdateManagerFactory.create(androidContext()) }
    single<FirebaseAuth> { FirebaseAuth.getInstance() }
    single { SnackbarManager }
    single<FirebaseAnalytics> { Firebase.analytics }
    single<BillingClient> { params ->
        BillingClient.newBuilder(androidContext())
            .setListener(params.get())
            .enablePendingPurchases()
            .build()
    }
    single<LocationManager> { LocationManagerImpl(get()) }
}

val settingsModule = module {
    single<UserDatastore> { UserDatastoreImpl(androidContext()) }
    single { UserPreferences(androidContext()) }
    single<WeatherPreferences> { WeatherPreferencesImpl(androidContext()) }
    single { NotesPreferencesImpl(androidContext()) }
    single<NotesPreferences> { NotesPreferencesImpl(androidContext()) }
    single<ConnectionManager> { ConnectionManagerImpl(androidContext()) }
}

val mainModule = module {
    viewModel { MainViewModel(repository = get()) }
    viewModel { LoginViewModel(repository = get()) }
    viewModel {
        MapViewModel(
            getUserPlacesUseCase = get(),
            getUserPlacesListUseCase = get(),
            addNewPlaceUseCase = get(),
            getFreeWeatherUseCase = get(),
            getFishActivityUseCase = get(),
            userPreferences = get(),
            locationManager = get(),
            getPlaceNameUseCase = get()
        )
    }

    viewModel {
        UserViewModel(
            userRepository = get(),
            repository = get(),
            getUserCatchUseCase = get(),
            userDatastore = get()
        )
    }
    viewModel {
        EditProfileViewModel(userDatastore = get(), userRepository = get())
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
            repository = get(),
            locationManager = get()
        )
    }
    viewModel {
        UserPlaceViewModel(
            markersRepo = get(),
            catchesRepo = get(),
            saveNewUserMarkerNoteUseCase = get(),
            deleteUserMarkerNoteUseCase = get()
        )
    }
    viewModel { UserCatchesViewModel(userCatchesUseCase = get()) }
    viewModel { UserPlacesViewModel(repository = get()) }
    viewModel { parameters ->
        NewCatchMasterViewModel(
            placeState = parameters.get(),
            getNewCatchWeatherUseCase = get(),
            saveNewCatchUseCase = get(),
            getUserPlacesListUseCase = get()
        )
    }
    viewModel { NotesViewModel(getUserCatches = get(), getUserPlacesList = get()) }
}

fun createGeocoder(androidContext: Context): Geocoder {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Geocoder(androidContext, androidContext.resources.configuration.locales[0])
    } else {
        Geocoder(androidContext)
    }
}
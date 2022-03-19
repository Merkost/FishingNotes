package com.mobileprism.fishing.di

import android.content.Context
import android.location.Geocoder
import com.android.billingclient.api.BillingClient
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.mobileprism.fishing.model.datastore.*
import com.mobileprism.fishing.model.datastore.impl.WeatherPreferencesImpl
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.use_cases.*
import com.mobileprism.fishing.ui.viewmodels.*
import com.mobileprism.fishing.utils.Logger
import com.mobileprism.fishing.utils.location.LocationManager
import com.mobileprism.fishing.utils.location.LocationManagerImpl
import com.mobileprism.fishing.utils.network.ConnectionManager
import com.mobileprism.fishing.utils.network.ConnectionManagerImpl
import com.mobileprism.fishing.viewmodels.MainViewModel
import com.mobileprism.fishing.viewmodels.MapViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
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
    single { AppPreferences(androidContext()) }
    single { UserPreferences(androidContext()) }
    single<WeatherPreferences> { WeatherPreferencesImpl(androidContext()) }
    single { NotesPreferencesImpl(androidContext()) }
    single<NotesPreferences> { NotesPreferencesImpl(androidContext()) }
    single<ConnectionManager> { ConnectionManagerImpl(androidContext()) }
}

val mainModule = module {

    viewModel { MainViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel {
        MapViewModel(
            repository = get(),
            getUserPlacesUseCase = get(),
            getUserPlacesListUseCase = get(),
            addNewPlaceUseCase = get(),
            getFreeWeatherUseCase = get(),
            getFishActivityUseCase = get(),
            geocoder = get(),
            userPreferences = get(),
            locationManager = get()
        )
    }

//    viewModel { NewCatchViewModel(get(), get(), get()) }

    viewModel { UserViewModel(userRepository = get(), repository = get()) }
    viewModel {
        UserCatchViewModel(
            markersRepository = get(),
            catchesRepository = get(named(CATCHES_REPOSITORY)),
            userRepository = get()
        )
    }
    viewModel { WeatherViewModel(get(), get()) }
    viewModel {
        UserPlaceViewModel(
            markersRepo = get(),
            catchesRepo = get(named(CATCHES_REPOSITORY)),
            saveNewUserMarkerNoteUseCase = get(),
            deleteUserMarkerNoteUseCase = get()
        )
    }
    viewModel { UserCatchesViewModel(get()) }
    viewModel { UserPlacesViewModel(get()) }
    viewModel { parameters ->
        NewCatchMasterViewModel(
            placeState = parameters.get(),
            get(),
            get(),
            get(),
        )
    }

}

fun createGeocoder(androidContext: Context): Geocoder {
    return Geocoder(androidContext, androidContext.resources.configuration.locales[0])
}
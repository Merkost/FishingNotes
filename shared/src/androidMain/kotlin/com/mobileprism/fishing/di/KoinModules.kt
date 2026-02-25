package com.mobileprism.fishing.di

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PendingPurchasesParams
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import com.mobileprism.fishing.domain.repository.SyncStatusProvider
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.model.datasource.firebase.FirebaseAnalyticsTracker
import com.mobileprism.fishing.model.datasource.local.sync.SyncStatusManager
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
import com.mobileprism.fishing.viewmodels.MapViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { Logger() }
    single<Geocoder> { createGeocoder(androidContext()) }
    single<AppUpdateManager> { AppUpdateManagerFactory.create(androidContext()) }
    single<FirebaseAuth> { FirebaseAuth.getInstance() }
    single { SnackbarManager }
    single<AnalyticsTracker> { FirebaseAnalyticsTracker(Firebase.analytics) }
    single<BillingClient> { params ->
        BillingClient.newBuilder(androidContext())
            .setListener(get())
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().build()
            )
            .build()
    }
    single<LocationManager> { LocationManagerImpl(get()) }
    single<SyncStatusProvider> { get<SyncStatusManager>() }
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
    viewModel { LoginViewModel(
        repository = get(),
        firebaseAuth = get(),
        analyticsTracker = get(),
        logger = get()
    ) }
    viewModel {
        MapViewModel(
            getUserPlacesListUseCase = get(),
            addNewPlaceUseCase = get(),
            getFreeWeatherUseCase = get(),
            getFishActivityUseCase = get(),
            userPreferences = get(),
            locationManager = get(),
            getPlaceNameUseCase = get()
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


fun createGeocoder(androidContext: Context): Geocoder {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Geocoder(androidContext, androidContext.resources.configuration.locales[0])
    } else {
        Geocoder(androidContext)
    }
}

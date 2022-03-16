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
import com.mobileprism.fishing.domain.use_cases.*
import com.mobileprism.fishing.model.datastore.*
import com.mobileprism.fishing.model.datastore.impl.WeatherPreferencesImpl
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.viewmodels.*
import com.mobileprism.fishing.utils.Logger
import com.mobileprism.fishing.utils.network.ConnectionManager
import com.mobileprism.fishing.utils.network.ConnectionManagerImpl
import com.mobileprism.fishing.viewmodels.MainViewModel
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
    single<FirebaseAnalytics> { Firebase.analytics }
    single<BillingClient> { params ->
        BillingClient.newBuilder(androidContext())
            .setListener(params.get())
            .enablePendingPurchases()
            .build()
    }
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
            getFreeWeatherUseCase = get(),
            getFishActivityUseCase = get(),
            geocoder = get(),
            userPreferences = get()
        )
    }
    viewModel { UserViewModel(get(), get()) }
    viewModel { parameters -> UserCatchViewModel(parameters.get(), get(), get(), get(), get()) }
    viewModel { WeatherViewModel(get(), get()) }
    viewModel { UserPlaceViewModel(get(), get()) }
    viewModel { UserCatchesViewModel(get()) }
    viewModel { UserPlacesViewModel(get()) }
    viewModel { parameters -> NewCatchMasterViewModel(parameters.get(), get(), get(), get()) }
}

val useCasesModule = module {
    factory { GetUserCatchesUseCase(get()) }
    factory { GetNewCatchWeatherUseCase(get(), get()) }
    factory { SaveNewCatchUseCase(get(), get(), get()) }
    factory { GetUserPlacesUseCase(get()) }
    factory { GetFishActivityUseCase(get()) }
    factory { GetFreeWeatherUseCase(get()) }
    factory { DeleteUserCatchUseCase(get(), get()) }
    factory { GetMapMarkerByIdUseCase(get()) }
    factory { SavePhotosUseCase(get()) }
    factory { UpdateUserCatchUseCase(get(), get()) }
}

fun createGeocoder(androidContext: Context): Geocoder {
    return Geocoder(androidContext, androidContext.resources.configuration.locales[0])
}
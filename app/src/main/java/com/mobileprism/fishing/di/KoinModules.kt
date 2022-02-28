package com.mobileprism.fishing.di

import com.android.billingclient.api.BillingClient
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.mobileprism.fishing.viewmodels.MainViewModel
import com.mobileprism.fishing.domain.*
import com.mobileprism.fishing.model.datastore.AppPreferences
import com.mobileprism.fishing.model.datastore.NotesPreferences
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.model.use_cases.GetNewCatchWeatherUseCase
import com.mobileprism.fishing.model.use_cases.GetUserCatchesUseCase
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.utils.Logger
import com.mobileprism.fishing.viewmodels.MapViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { Logger() }
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
    single { WeatherPreferences(androidContext()) }
    single { NotesPreferences(androidContext()) }
}

val mainModule = module {

    viewModel { MainViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { MapViewModel(get(), get(), get(), get()) }

    viewModel { NewCatchViewModel(get(), get(), get()) }

    viewModel { UserViewModel(get(), get()) }
    viewModel { UserCatchViewModel(get(), get(), get()) }
    viewModel { WeatherViewModel(get(), get()) }
    viewModel { UserPlaceViewModel(get(), get()) }
    viewModel { UserCatchesViewModel(get()) }
    viewModel { UserPlacesViewModel(get()) }
    viewModel { parameters ->
        NewCatchMasterViewModel(
            placeState = parameters.get(),
            get(),
            get(),
            get(),
            get()
        )
    }

}

val useCasesModule = module {
    single { GetUserCatchesUseCase(get()) }
    single { GetNewCatchWeatherUseCase(get()) }
}
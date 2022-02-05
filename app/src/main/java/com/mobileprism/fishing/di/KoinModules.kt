package com.mobileprism.fishing.di

import com.android.billingclient.api.BillingClient
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.mobileprism.fishing.compose.ui.home.SnackbarManager
import com.mobileprism.fishing.compose.viewmodels.MainViewModel
import com.mobileprism.fishing.compose.viewmodels.MapViewModel
import com.mobileprism.fishing.domain.*
import com.mobileprism.fishing.model.datastore.AppPreferences
import com.mobileprism.fishing.model.datastore.NotesPreferences
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.utils.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { Logger() }
    single { SnackbarManager }
    single { Firebase.analytics }
    single { params ->
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

    viewModel { MapViewModel(get(), get(), get()) }
    viewModel { NewCatchViewModel(get(), get(), get()) }
    viewModel { UserViewModel(get(), get()) }
    viewModel { NewPlaceViewModel(get()) }
    viewModel { UserCatchViewModel(get(), get(), get()) }
    viewModel { WeatherViewModel(get(), get()) }
    viewModel { UserPlaceViewModel(get(), get()) }
    viewModel { UserCatchesViewModel(get()) }
    viewModel { UserPlacesViewModel(get()) }

}
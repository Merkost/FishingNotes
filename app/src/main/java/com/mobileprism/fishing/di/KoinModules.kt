package com.mobileprism.fishing.di

import com.mobileprism.fishing.compose.ui.home.SnackbarManager
import com.mobileprism.fishing.compose.viewmodels.MainViewModel
import com.mobileprism.fishing.compose.viewmodels.MapViewModel
import com.mobileprism.fishing.domain.*
import com.mobileprism.fishing.model.datasource.FreeWeatherRepositoryImpl
import com.mobileprism.fishing.model.datasource.SolunarRetrofitRepositoryImpl
import com.mobileprism.fishing.model.datasource.WeatherRepositoryRetrofitImpl
import com.mobileprism.fishing.model.datasource.firebase.*
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.model.datastore.AppPreferences
import com.mobileprism.fishing.model.datastore.NotesPreferences
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.model.repository.PhotoStorage
import com.mobileprism.fishing.model.repository.UserRepository
import com.mobileprism.fishing.model.repository.app.*
import com.mobileprism.fishing.utils.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { Logger() }
    single { SnackbarManager }
}

val repositoryModule = module {
    single { RepositoryCollections() }

    single<UserRepository> { FirebaseUserRepositoryImpl(get(), get(), androidContext()) }
    single<CatchesRepository> { FirebaseCatchesRepositoryImpl(get(), get()) }
    single<MarkersRepository> { FirebaseMarkersRepositoryImpl(get(), get()) }
    single<SolunarRepository> { SolunarRetrofitRepositoryImpl() }
    single<PhotoStorage> { FirebaseCloudPhotoStorage(androidContext()) }
    single<WeatherRepository> { WeatherRepositoryRetrofitImpl() }
    single<FreeWeatherRepository> { FreeWeatherRepositoryImpl() }
    single<OfflineRepository> { FirebaseOfflineRepositoryImpl(dbCollections = get()) }
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
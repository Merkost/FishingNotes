package com.mobileprism.fishing.di

import com.mobileprism.fishing.model.datasource.FreeWeatherRepositoryImpl
import com.mobileprism.fishing.model.datasource.SolunarRetrofitRepositoryImpl
import com.mobileprism.fishing.model.datasource.WeatherRepositoryRetrofitImpl
import com.mobileprism.fishing.model.datasource.firebase.*
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.model.repository.PhotoStorage
import com.mobileprism.fishing.model.repository.UserRepository
import com.mobileprism.fishing.model.repository.app.*
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single { RepositoryCollections() }

    single<UserRepository> {
        FirebaseUserRepositoryImpl(
            appPreferences = get(),
            dbCollections = get(),
            firebaseAnalytics = get(),
            context = androidContext()
        )
    }
    single<CatchesRepository> {
        FirebaseCatchesRepositoryImpl(
            dbCollections = get(),
            firebaseAnalytics = get(),
            cloudPhotoStorage = get()
        )
    }
    single<MarkersRepository> {
        FirebaseMarkersRepositoryImpl(
            dbCollections = get(),
            firebaseAnalytics = get(),
            cloudPhotoStorage = get()
        )
    }
    single<SolunarRepository> { SolunarRetrofitRepositoryImpl(firebaseAnalytics = get()) }
    single<PhotoStorage> {
        FirebaseCloudPhotoStorage(
            firebaseAnalytics = get(),
            context = androidContext()
        )
    }
    single<WeatherRepository> { WeatherRepositoryRetrofitImpl(firebaseAnalytics = get()) }
    single<FreeWeatherRepository> { FreeWeatherRepositoryImpl(firebaseAnalytics = get()) }
    single<OfflineRepository> { FirebaseOfflineRepositoryImpl(dbCollections = get()) }
}
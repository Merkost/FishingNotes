package com.mobileprism.fishing.di

import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.domain.repository.FirebaseUserRepository
import com.mobileprism.fishing.domain.repository.app.FreeWeatherRepository
import com.mobileprism.fishing.domain.repository.app.OfflineRepository
import com.mobileprism.fishing.domain.repository.app.SolunarRepository
import com.mobileprism.fishing.domain.repository.app.WeatherRepository
import com.mobileprism.fishing.model.auth.AuthManagerImpl
import com.mobileprism.fishing.model.datasource.FreeWeatherRepositoryImpl
import com.mobileprism.fishing.model.datasource.SolunarRetrofitRepositoryImpl
import com.mobileprism.fishing.model.datasource.WeatherRepositoryRetrofitImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseOfflineRepositoryImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseUserRepositoryImpl
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val loginModule = module {
    single { RepositoryCollections() }


    single<FirebaseUserRepository> {
        FirebaseUserRepositoryImpl(
            userDatastore = get(),
            dbCollections = get(),
            firebaseAnalytics = get(),
            context = androidContext()
        )
    }
    single<SolunarRepository> { SolunarRetrofitRepositoryImpl(firebaseAnalytics = get()) }
    single<WeatherRepository> {
        WeatherRepositoryRetrofitImpl(
            firebaseAnalytics = get(),
            okHttpClient = get()
        )
    }
    single<FreeWeatherRepository> { FreeWeatherRepositoryImpl(firebaseAnalytics = get()) }
    single<OfflineRepository> { FirebaseOfflineRepositoryImpl(dbCollections = get()) }
    single<AuthManager> {
        AuthManagerImpl(
            userDatastore = get(),
            userRepository = get(),
            firebaseUserRepository = get(),
            tokenStore = get()
        )
    }
}
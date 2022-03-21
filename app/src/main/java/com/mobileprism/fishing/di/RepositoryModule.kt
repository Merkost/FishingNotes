package com.mobileprism.fishing.di

import com.mobileprism.fishing.BuildConfig
import com.mobileprism.fishing.domain.repository.PhotoStorage
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.app.*
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.model.datasource.FreeWeatherRepositoryImpl
import com.mobileprism.fishing.model.datasource.SolunarRetrofitRepositoryImpl
import com.mobileprism.fishing.model.datasource.WeatherRepositoryRetrofitImpl
import com.mobileprism.fishing.model.datasource.firebase.*
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val userRepositoryModule = module {
    single<UserRepository> {
        FirebaseUserRepositoryImpl(
            appPreferences = get(),
            dbCollections = get(),
            firebaseAnalytics = get(),
            context = androidContext()
        )
    }
}

val repositoryModule = module {
    single { RepositoryCollections() }

    single<CatchesRepository> {
        FirebaseCatchesRepositoryImpl(
            dbCollections = get(),
            firebaseAnalytics = get(),
            connectionManager = get()
        )
    }
    single<MarkersRepository> {
        FirebaseMarkersRepositoryImpl(
            dbCollections = get(),
            firebaseAnalytics = get(),
            context = androidContext()
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

fun createLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().setLevel(
        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
    )
}

/**
 * Create a OkHttpClient which is used to send HTTP requests and read their responses.
 * @loggingInterceptor logging interceptor
 */
private fun createOkHttpClient(
    loggingInterceptor: HttpLoggingInterceptor,
): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
}

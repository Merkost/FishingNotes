package com.mobileprism.fishing.di

import com.mobileprism.fishing.BuildConfig
import com.mobileprism.fishing.model.datasource.FreeWeatherRepositoryImpl
import com.mobileprism.fishing.model.datasource.SolunarRetrofitRepositoryImpl
import com.mobileprism.fishing.model.datasource.WeatherRepositoryRetrofitImpl
import com.mobileprism.fishing.model.datasource.firebase.*
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.model.repository.PhotoStorage
import com.mobileprism.fishing.model.repository.UserRepository
import com.mobileprism.fishing.model.repository.app.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

const val CATCHES_REPOSITORY = "Catches repository"
const val CATCHES_REPOSITORY_OFFLINE = "Catches repository offline"

val repositoryModule = module {
    single { RepositoryCollections() }
    //Create HttpLoggingInterceptor
    //single<HttpLoggingInterceptor> { createLoggingInterceptor() }
    //Create OkHttpClient
    //single<OkHttpClient> { createOkHttpClient(get()) }


    single<UserRepository> {
        FirebaseUserRepositoryImpl(
            appPreferences = get(),
            dbCollections = get(),
            firebaseAnalytics = get(),
            context = androidContext()
        )
    }
    single<CatchesRepository>(named(CATCHES_REPOSITORY)) {
        FirebaseCatchesRepositoryImpl(
            dbCollections = get(),
            firebaseAnalytics = get(),
            cloudPhotoStorage = get()
        )
    }
    /*single<CatchesRepository>(named(CATCHES_REPOSITORY_OFFLINE)) {
        FirebaseCatchesRepositoryOfflineImpl(
            dbCollections = get(),
            firebaseAnalytics = get(),
        )
    }*/
    /*single<CatchesRepository> {
        FirebaseCatchesRepositoryImpl(
            dbCollections = get(),
            firebaseAnalytics = get(),
            cloudPhotoStorage = get(),
            context = androidContext()
        )
    }*/
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

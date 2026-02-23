package com.mobileprism.fishing.di

import androidx.room.Room
import com.mobileprism.fishing.BuildConfig
import com.mobileprism.fishing.domain.repository.PhotoStorage
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.app.FreeWeatherRepository
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.domain.repository.app.OfflineRepository
import com.mobileprism.fishing.domain.repository.app.SolunarRepository
import com.mobileprism.fishing.domain.repository.app.WeatherRepository
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.model.datasource.FreeWeatherRepositoryImpl
import com.mobileprism.fishing.model.datasource.SolunarRetrofitRepositoryImpl
import com.mobileprism.fishing.model.datasource.WeatherRepositoryRetrofitImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseCatchesRepositoryImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseCloudPhotoStorage
import com.mobileprism.fishing.model.datasource.firebase.FirebaseMarkersRepositoryImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseOfflineRepositoryImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseUserRepositoryImpl
import com.mobileprism.fishing.model.datasource.local.CachedWeatherRepository
import com.mobileprism.fishing.model.datasource.local.FishingDatabase
import com.mobileprism.fishing.model.datasource.local.SyncAwareCatchesRepository
import com.mobileprism.fishing.model.datasource.local.SyncAwareMarkersRepository
import com.mobileprism.fishing.model.datasource.local.sync.SyncScheduler
import com.mobileprism.fishing.model.datasource.local.sync.SyncStatusManager
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose
import java.io.Closeable
import java.util.concurrent.TimeUnit

val userRepositoryModule = module {
    single<UserRepository> {
        FirebaseUserRepositoryImpl(
            userDatastore = get(),
            dbCollections = get(),
            analyticsTracker = get(),
            context = androidContext()
        )
    }
}

val repositoryModule = module {
    single { RepositoryCollections() }

    single<OkHttpClient> { createOkHttpClient() }

    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            FishingDatabase::class.java,
            "fishing_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAOs
    single { get<FishingDatabase>().catchDao() }
    single { get<FishingDatabase>().markerDao() }
    single { get<FishingDatabase>().pendingOperationDao() }
    single { get<FishingDatabase>().weatherCacheDao() }

    // Sync infrastructure
    single { SyncScheduler(androidContext()) }
    single {
        SyncStatusManager(pendingOpsDao = get(), connectionManager = get())
    } onClose { (it as? Closeable)?.close() }

    // Firebase implementations (named)
    single<CatchesRepository>(named("firebase")) {
        FirebaseCatchesRepositoryImpl(
            dbCollections = get(),
            analyticsTracker = get(),
            connectionManager = get()
        )
    }
    single<MarkersRepository>(named("firebase")) {
        FirebaseMarkersRepositoryImpl(
            dbCollections = get(),
            analyticsTracker = get(),
            context = androidContext()
        )
    }
    single<WeatherRepository>(named("remote")) {
        WeatherRepositoryRetrofitImpl(
            analyticsTracker = get(),
            openWeatherKey = BuildConfig.OPENWEATHER_KEY,
            okHttpClient = get()
        )
    }

    // SyncAware wrappers (default bindings)
    single<CatchesRepository> {
        SyncAwareCatchesRepository(
            firebaseRepo = get(named("firebase")),
            catchDao = get(),
            pendingOpsDao = get(),
            connectionManager = get(),
            syncScheduler = get()
        )
    } onClose { (it as? Closeable)?.close() }
    single<MarkersRepository> {
        SyncAwareMarkersRepository(
            firebaseRepo = get(named("firebase")),
            markerDao = get(),
            pendingOpsDao = get(),
            connectionManager = get(),
            syncScheduler = get()
        )
    } onClose { (it as? Closeable)?.close() }
    single<WeatherRepository> {
        CachedWeatherRepository(
            remoteRepo = get(named("remote")),
            weatherCacheDao = get(),
            connectionManager = get()
        )
    }

    single<SolunarRepository> {
        SolunarRetrofitRepositoryImpl(
            analyticsTracker = get(),
            okHttpClient = get()
        )
    }
    single<PhotoStorage> {
        FirebaseCloudPhotoStorage(
            analyticsTracker = get(),
            context = androidContext()
        )
    }
    single<FreeWeatherRepository> {
        FreeWeatherRepositoryImpl(
            analyticsTracker = get(),
            rapidApiKey = BuildConfig.RAPIDAPI_KEY,
            okHttpClient = get()
        )
    }
    single<OfflineRepository> { FirebaseOfflineRepositoryImpl(dbCollections = get()) }
}

private fun createOkHttpClient(): OkHttpClient {
    val loggingInterceptor = HttpLoggingInterceptor().setLevel(
        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
    )
    return OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
}

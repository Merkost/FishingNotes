package com.mobileprism.fishing.di

import androidx.core.os.LocaleListCompat
import androidx.room.Room
import com.mobileprism.fishing.BuildKonfig
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.domain.repository.PhotoStorage
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.model.datasource.firebase.FirebaseAuthRepository
import com.mobileprism.fishing.domain.repository.app.FreeWeatherRepository
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.domain.repository.app.MarkersRepositoryPaged
import com.mobileprism.fishing.domain.repository.app.OfflineRepository
import com.mobileprism.fishing.domain.repository.app.SolunarRepository
import com.mobileprism.fishing.domain.repository.app.WeatherRepository
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryRead
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryUpdate
import com.mobileprism.fishing.model.datasource.FreeWeatherRepositoryKtorImpl
import com.mobileprism.fishing.model.datasource.SolunarRepositoryKtorImpl
import com.mobileprism.fishing.model.datasource.WeatherRepositoryKtorImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseCatchesPagedRepository
import com.mobileprism.fishing.model.datasource.firebase.FirebaseCatchesRepositoryImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseCloudPhotoStorage
import com.mobileprism.fishing.model.datasource.firebase.FirebaseMarkersPagedRepository
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
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose
import java.io.Closeable

val repositoryModule = networkModule + module {
    single<AuthRepository> { FirebaseAuthRepository() }
    single { RepositoryCollections(authRepository = get()) }

    // User repository (no Android Context needed)
    single<UserRepository> {
        FirebaseUserRepositoryImpl(
            userDatastore = get(),
            dbCollections = get(),
            analyticsTracker = get()
        )
    }

    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            FishingDatabase::class.java,
            "fishing_database"
        ).build()
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

    // Core Firebase implementations (commonMain)
    single { FirebaseCatchesRepositoryImpl(
        dbCollections = get(),
        analyticsTracker = get(),
        connectionManager = get(),
        authRepository = get()
    ) } onClose { (it as? Closeable)?.close() }
    single { FirebaseMarkersRepositoryImpl(
        dbCollections = get(),
        analyticsTracker = get()
    ) } onClose { (it as? Closeable)?.close() }

    // Paged wrappers (androidMain, named bindings)
    single<CatchesRepository>(named("firebase")) {
        FirebaseCatchesPagedRepository(
            coreRepo = get(),
            authRepository = get()
        )
    }
    single<MarkersRepositoryPaged>(named("firebase")) {
        FirebaseMarkersPagedRepository(
            coreRepo = get(),
            authRepository = get()
        )
    }
    single<WeatherRepository>(named("remote")) {
        WeatherRepositoryKtorImpl(
            analyticsTracker = get(),
            openWeatherKey = BuildKonfig.OPENWEATHER_KEY,
            languageTag = LocaleListCompat.getAdjustedDefault().toLanguageTags().take(2),
            httpClient = get()
        )
    }

    // SyncAware wrappers (default bindings)
    single<CatchesRepository> {
        SyncAwareCatchesRepository(
            firebaseRepo = get(named("firebase")),
            catchDao = get(),
            pendingOpsDao = get(),
            connectionManager = get(),
            syncScheduler = get(),
            db = get()
        )
    } onClose { (it as? Closeable)?.close() }
    single<MarkersRepositoryPaged> {
        SyncAwareMarkersRepository(
            firebaseRepo = get(named("firebase")),
            markerDao = get(),
            pendingOpsDao = get(),
            connectionManager = get(),
            syncScheduler = get(),
            db = get()
        )
    } onClose { (it as? Closeable)?.close() }
    // Also bind as MarkersRepository so consumers that don't need paging can inject the base type
    single<MarkersRepository> { get<MarkersRepositoryPaged>() }
    // Also bind as base interfaces for consumers that don't need paging
    single<CatchesRepositoryRead> { get<CatchesRepository>() }
    single<CatchesRepositoryUpdate> { get<CatchesRepository>() }
    single<WeatherRepository> {
        CachedWeatherRepository(
            remoteRepo = get(named("remote")),
            weatherCacheDao = get(),
            connectionManager = get()
        )
    }

    single<SolunarRepository> {
        SolunarRepositoryKtorImpl(
            analyticsTracker = get(),
            httpClient = get()
        )
    }
    single<PhotoStorage> {
        FirebaseCloudPhotoStorage(
            analyticsTracker = get(),
            context = androidContext()
        )
    }
    single<FreeWeatherRepository> {
        FreeWeatherRepositoryKtorImpl(
            analyticsTracker = get(),
            rapidApiKey = BuildKonfig.RAPIDAPI_KEY,
            httpClient = get()
        )
    }
    single<OfflineRepository> { FirebaseOfflineRepositoryImpl(dbCollections = get()) }
}

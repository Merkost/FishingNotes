package com.mobileprism.fishing.di

import com.mobileprism.fishing.BuildKonfig
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.SyncStatusProvider
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
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
import com.mobileprism.fishing.model.datasource.firebase.FirebaseAuthRepository
import com.mobileprism.fishing.model.datasource.firebase.FirebaseAnalyticsTracker
import com.mobileprism.fishing.model.datasource.firebase.FirebaseCatchesPagedRepository
import com.mobileprism.fishing.model.datasource.firebase.FirebaseCatchesRepositoryImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseMarkersPagedRepository
import com.mobileprism.fishing.model.datasource.firebase.FirebaseMarkersRepositoryImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseOfflineRepositoryImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseUserRepositoryImpl
import com.mobileprism.fishing.model.datasource.local.CachedWeatherRepository
import com.mobileprism.fishing.model.datasource.local.FishingDatabase
import com.mobileprism.fishing.model.datasource.local.SyncAwareCatchesRepository
import com.mobileprism.fishing.model.datasource.local.SyncAwareMarkersRepository
import com.mobileprism.fishing.model.datasource.local.sync.SyncStatusManager
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.ui.home.SnackbarManager
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose

val commonRepositoryModule = module {
    single { SnackbarManager }
    single<AnalyticsTracker> { FirebaseAnalyticsTracker() }
    single<SyncStatusProvider> { get<SyncStatusManager>() }
    single<AuthRepository> { FirebaseAuthRepository() }
    single { RepositoryCollections(authRepository = get()) }

    single<UserRepository> {
        FirebaseUserRepositoryImpl(
            userDatastore = get(),
            dbCollections = get(),
            analyticsTracker = get()
        )
    }

    single { get<FishingDatabase>().catchDao() }
    single { get<FishingDatabase>().markerDao() }
    single { get<FishingDatabase>().pendingOperationDao() }
    single { get<FishingDatabase>().weatherCacheDao() }

    single {
        SyncStatusManager(pendingOpsDao = get(), connectionManager = get())
    } onClose { (it as? AutoCloseable)?.close() }

    single { FirebaseCatchesRepositoryImpl(
        dbCollections = get(),
        analyticsTracker = get(),
        connectionManager = get(),
        authRepository = get()
    ) } onClose { (it as? AutoCloseable)?.close() }
    single { FirebaseMarkersRepositoryImpl(
        dbCollections = get(),
        analyticsTracker = get()
    ) } onClose { (it as? AutoCloseable)?.close() }

    single<CatchesRepository>(named("firebase")) {
        FirebaseCatchesPagedRepository(
            coreRepo = get(),
            authRepository = get(),
            dbCollections = get()
        )
    }
    single<MarkersRepositoryPaged>(named("firebase")) {
        FirebaseMarkersPagedRepository(
            coreRepo = get(),
            authRepository = get(),
            dbCollections = get()
        )
    }

    single<CatchesRepository> {
        SyncAwareCatchesRepository(
            firebaseRepo = get(named("firebase")),
            catchDao = get(),
            pendingOpsDao = get(),
            connectionManager = get(),
            syncScheduler = get(),
            db = get()
        )
    } onClose { (it as? AutoCloseable)?.close() }
    single<MarkersRepositoryPaged> {
        SyncAwareMarkersRepository(
            firebaseRepo = get(named("firebase")),
            markerDao = get(),
            pendingOpsDao = get(),
            connectionManager = get(),
            syncScheduler = get(),
            db = get()
        )
    } onClose { (it as? AutoCloseable)?.close() }
    single<MarkersRepository> { get<MarkersRepositoryPaged>() }
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
    single<FreeWeatherRepository> {
        FreeWeatherRepositoryKtorImpl(
            analyticsTracker = get(),
            rapidApiKey = BuildKonfig.RAPIDAPI_KEY,
            httpClient = get()
        )
    }
    single<OfflineRepository> { FirebaseOfflineRepositoryImpl(dbCollections = get()) }
}

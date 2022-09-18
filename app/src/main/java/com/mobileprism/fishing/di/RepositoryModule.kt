package com.mobileprism.fishing.di

import androidx.room.Room
import com.mobileprism.fishing.domain.repository.PhotoStorage
import com.mobileprism.fishing.domain.repository.app.*
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.model.datasource.FreeWeatherRepositoryImpl
import com.mobileprism.fishing.model.datasource.SolunarRetrofitRepositoryImpl
import com.mobileprism.fishing.model.datasource.WeatherRepositoryRetrofitImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseCatchesRepositoryImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseCloudPhotoStorage
import com.mobileprism.fishing.model.datasource.firebase.FirebaseMarkersRepositoryImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseOfflineRepositoryImpl
import com.mobileprism.fishing.model.datasource.room.FishingDatabase
import com.mobileprism.fishing.model.datasource.room.LocalCatchesRepositoryImpl
import com.mobileprism.fishing.model.datasource.room.LocalCloudPhotoStorage
import com.mobileprism.fishing.model.datasource.room.LocalMarkersRepositoryImpl
import com.mobileprism.fishing.model.datasource.room.dao.CatchesDao
import com.mobileprism.fishing.model.datasource.room.dao.MapMarkersDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val repositoryModuleFirebase = module {

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
    single<PhotoStorage> {
        FirebaseCloudPhotoStorage(
            firebaseAnalytics = get(),
            context = androidContext()
        )
    }

}

val repositoryModuleLocal = module {

    single<FishingDatabase> {
        Room.databaseBuilder(get(), FishingDatabase::class.java, "Fishing.db")
            .fallbackToDestructiveMigration().build()
    }
    single<CatchesDao> { get<FishingDatabase>().catchesDao() }
    single<MapMarkersDao> { get<FishingDatabase>().mapMarkersDao() }


/*    single<FirebaseUserRepository> {
        LocalUserRepositoryImpl(
            userDatastore = get(),
            firebaseAnalytics = get(),
            context = androidContext()
        )
    }*/

    single<CatchesRepository> {
        LocalCatchesRepositoryImpl(
            catchesDao = get(),
            firebaseAnalytics = get(),
        )
    }
    single<MarkersRepository> {
        LocalMarkersRepositoryImpl(
            markersDao = get(),
            firebaseAnalytics = get(),
        )
    }

    single<PhotoStorage> {
        LocalCloudPhotoStorage(
            firebaseAnalytics = get(),
            context = androidContext()
        )
    }


    single<SolunarRepository> { SolunarRetrofitRepositoryImpl(firebaseAnalytics = get()) }

    single<WeatherRepository> {
        WeatherRepositoryRetrofitImpl(
            firebaseAnalytics = get(),
            okHttpClient = createOkHttpClient(createLoggingInterceptor())
        )
    }

    single<FreeWeatherRepository> { FreeWeatherRepositoryImpl(firebaseAnalytics = get()) }

    single<OfflineRepository> { FirebaseOfflineRepositoryImpl(dbCollections = get()) }

}



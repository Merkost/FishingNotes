package com.mobileprism.fishing.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mobileprism.fishing.BuildKonfig
import com.mobileprism.fishing.domain.repository.PhotoStorage
import com.mobileprism.fishing.domain.repository.app.WeatherRepository
import com.mobileprism.fishing.model.datasource.WeatherRepositoryKtorImpl
import com.mobileprism.fishing.model.datasource.firebase.IosPhotoStorage
import com.mobileprism.fishing.model.datasource.local.FishingDatabase
import com.mobileprism.fishing.model.datasource.local.sync.IosSyncScheduler
import com.mobileprism.fishing.model.datasource.local.sync.SyncScheduler
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
val iosPlatformRepositoryModule = module {
    single {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )!!
        val dbFilePath = "${documentDirectory.path}/fishing_database"
        Room.databaseBuilder<FishingDatabase>(
            name = dbFilePath,
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    single<SyncScheduler> { IosSyncScheduler() }

    single<WeatherRepository>(named("remote")) {
        WeatherRepositoryKtorImpl(
            analyticsTracker = get(),
            openWeatherKey = BuildKonfig.OPENWEATHER_KEY,
            languageTag = "en",
            httpClient = get()
        )
    }

    single<PhotoStorage> { IosPhotoStorage() }
}

val repositoryModule = commonRepositoryModule + iosPlatformRepositoryModule

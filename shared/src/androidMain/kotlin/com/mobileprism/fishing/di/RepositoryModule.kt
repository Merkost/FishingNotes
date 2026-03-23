@file:JvmName("RepositoryModuleAndroid")
package com.mobileprism.fishing.di

import androidx.core.os.LocaleListCompat
import androidx.room.Room
import com.mobileprism.fishing.BuildKonfig
import com.mobileprism.fishing.domain.repository.PhotoStorage
import com.mobileprism.fishing.domain.repository.app.WeatherRepository
import com.mobileprism.fishing.model.datasource.WeatherRepositoryKtorImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseCloudPhotoStorage
import com.mobileprism.fishing.model.datasource.local.FishingDatabase
import com.mobileprism.fishing.model.datasource.local.sync.SyncScheduler
import com.mobileprism.fishing.model.datasource.local.sync.SyncSchedulerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val platformRepositoryModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            FishingDatabase::class.java,
            "fishing_database"
        ).build()
    }

    single<SyncScheduler> { SyncSchedulerImpl(androidContext()) }

    single<WeatherRepository>(named("remote")) {
        WeatherRepositoryKtorImpl(
            analyticsTracker = get(),
            openWeatherKey = BuildKonfig.OPENWEATHER_KEY,
            languageTag = LocaleListCompat.getAdjustedDefault().toLanguageTags().take(2),
            httpClient = get()
        )
    }

    single<PhotoStorage> {
        FirebaseCloudPhotoStorage(
            analyticsTracker = get(),
            context = androidContext()
        )
    }
}

actual val repositoryModule = commonRepositoryModule + platformRepositoryModule

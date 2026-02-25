package com.mobileprism.fishing.app

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import com.mobileprism.fishing.BuildKonfig
import com.mobileprism.fishing.di.*
import com.mobileprism.fishing.model.datasource.local.sync.SyncScheduler
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


class FishingApp : Application() {

    override fun onCreate() {
        super.onCreate()

        GoogleAuthProvider.create(GoogleAuthCredentials(serverId = BuildKonfig.GOOGLE_WEB_CLIENT_ID))

        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .crossfade(true)
                .memoryCache {
                    MemoryCache.Builder(this)
                        .maxSizePercent(0.15)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir.resolve("image_cache"))
                        .maxSizePercent(0.05)
                        .build()
                }
                .build()
        )

        startKoin {
            androidLogger(if (BuildKonfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@FishingApp)
            workManagerFactory()
            modules(
                appModule,
                mainModule,
                repositoryModule,
                settingsModule,
                useCasesModule
            )
        }

        // Schedule periodic sync as safety net
        get<SyncScheduler>().schedulePeriodicSync()
    }

}

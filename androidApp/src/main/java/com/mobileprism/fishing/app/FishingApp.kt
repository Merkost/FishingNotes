package com.mobileprism.fishing.app

import android.app.Application
import android.content.pm.ApplicationInfo
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import com.mobileprism.fishing.BuildInfo
import com.mobileprism.fishing.BuildKonfig
import com.mobileprism.fishing.di.sharedModules
import com.mobileprism.fishing.model.datasource.local.sync.SyncScheduler
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.kimplify.cedar.logging.Cedar
import org.kimplify.cedar.logging.trees.PlatformLogTree
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module


class FishingApp : Application(), SingletonImageLoader.Factory {

    override fun newImageLoader(context: coil3.PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.15)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        if (isDebug) {
            Cedar.plant(PlatformLogTree())
        }

        GoogleAuthProvider.create(GoogleAuthCredentials(serverId = BuildKonfig.GOOGLE_WEB_CLIENT_ID))

        startKoin {
            androidLogger(if (isDebug) Level.ERROR else Level.NONE)
            androidContext(this@FishingApp)
            workManagerFactory()
            modules(sharedModules + module { single { BuildInfo(isDebug) } })
        }

        // Schedule periodic sync as safety net
        get<SyncScheduler>().schedulePeriodicSync()
    }

}

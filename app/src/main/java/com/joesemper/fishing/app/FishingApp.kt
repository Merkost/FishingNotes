package com.joesemper.fishing.app

import android.app.Application
import com.joesemper.fishing.di.appModule
import com.joesemper.fishing.di.groupsScreen
import com.joesemper.fishing.di.splashScreen
import com.joesemper.fishing.di.weatherScreen
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class FishingApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin{
            androidLogger()
            androidContext(this@FishingApp)
            modules(listOf(appModule, splashScreen, weatherScreen, groupsScreen))
        }
    }
}
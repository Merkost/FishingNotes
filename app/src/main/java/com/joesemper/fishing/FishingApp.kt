package com.joesemper.fishing

import android.app.Application
import com.joesemper.fishing.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class FishingApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@FishingApp)
            modules(
                listOf(
                    appModule,
                    splashScreen,
                    loginScreen,
                    mapScreen,
                    weatherScreen,
                    mainActivity,
                    markerFragment,
                    newCatchFragment,
                    catchesInnerFragment,
                    userCatchFragment,
                    userFragment,
                    notesFragment
                )
            )
        }
    }
}
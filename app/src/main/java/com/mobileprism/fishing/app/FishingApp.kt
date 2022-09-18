package com.mobileprism.fishing.app

import android.app.Application
import com.mobileprism.fishing.BuildConfig
import com.mobileprism.fishing.di.*
import com.mobileprism.fishing.utils.Constants
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


class FishingApp : Application() {

    override fun onCreate() {
        super.onCreate()

        //Get device info
        /*Log.d("DEVICE_INFO",
            android.os.Build::class.java.fields.map { "Build.${it.name} = ${it.get(it.name)}" }
                .joinToString("\n")
        )*/

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@FishingApp)
            modules(
                appModule,
                mainModule,
                networkModule,

                fishingApiModule,
                if (Constants.FAKE_API) fishingFakeRepositoryModule else fishingRepository,
                fishingNetworkModule,

                loginModule,
                settingsModule,
                useCasesModule,
                repositoryModuleLocal
            )

        }
        // FIXME: Reset koin
    }

}
package com.mobileprism.fishing.di

import org.koin.core.context.startKoin

fun initKoinIos() {
    startKoin {
        modules(
            listOf(
                appModule,
                settingsModule,
                mainModule,
                useCasesModule,
            ) + repositoryModule
        )
    }
}

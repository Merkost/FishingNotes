package com.mobileprism.fishing.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import org.koin.core.context.startKoin

fun initKoinIos() {
    Firebase.initialize()
    startKoin {
        modules(sharedModules)
    }
}

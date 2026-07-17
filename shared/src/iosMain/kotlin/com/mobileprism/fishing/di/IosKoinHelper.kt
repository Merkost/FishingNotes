package com.mobileprism.fishing.di

import com.mobileprism.fishing.BuildInfo
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import org.koin.core.context.startKoin
import org.koin.dsl.module

@OptIn(ExperimentalNativeApi::class)
fun initKoinIos() {
    Firebase.initialize()
    startKoin {
        modules(sharedModules + module { single { BuildInfo(Platform.isDebugBinary) } })
    }
}

package com.mobileprism.fishing.di

import org.koin.core.module.Module

expect val appModule: Module
expect val settingsModule: Module
expect val repositoryModule: List<Module>

val sharedModules: List<Module>
    get() = listOf(appModule, settingsModule, commonViewModelsModule, useCasesModule, networkModule) + repositoryModule

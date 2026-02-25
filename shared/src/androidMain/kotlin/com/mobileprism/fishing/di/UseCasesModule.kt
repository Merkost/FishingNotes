package com.mobileprism.fishing.di

import com.mobileprism.fishing.domain.use_cases.GetPlaceNameUseCase
import com.mobileprism.fishing.domain.use_cases.catches.SaveNewCatchUseCase
import org.koin.dsl.module

val useCasesModule = commonUseCasesModule + module {
    factory { SaveNewCatchUseCase(get(), get(), get(), get()) }
    factory { GetPlaceNameUseCase(get()) }
}

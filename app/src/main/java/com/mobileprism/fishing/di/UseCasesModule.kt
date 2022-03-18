package com.mobileprism.fishing.di

import com.mobileprism.fishing.ui.use_cases.*
import com.mobileprism.fishing.ui.use_cases.notes.DeleteUserMarkerNoteUseCase
import com.mobileprism.fishing.ui.use_cases.notes.SaveUserMarkerNoteUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val useCasesModule = module {

    factory {
        GetUserCatchesUseCase(
            repository = get(named(CATCHES_REPOSITORY))
        )
    }

    factory {
        GetNewCatchWeatherUseCase(
            weatherRepository = get(),
            weatherPreferences = get()
        )
    }

    factory {
        SaveNewCatchUseCase(
            catchesRepository = get(named(CATCHES_REPOSITORY)),
            catchesRepositoryOffline = get(named(CATCHES_REPOSITORY_OFFLINE)),
            photosRepository = get(),
            connectionManager = get(),
            weatherPreferences = get()
        )
    }

    factory { AddNewPlaceUseCase(get()) }
    factory { GetUserPlacesUseCase(get()) }

    factory { GetUserPlacesListUseCase(get()) }
    factory { GetFishActivityUseCase(get()) }
    factory { GetFreeWeatherUseCase(get()) }

    factory { SaveUserMarkerNoteUseCase(get()) }
    factory { DeleteUserMarkerNoteUseCase(get()) }

}
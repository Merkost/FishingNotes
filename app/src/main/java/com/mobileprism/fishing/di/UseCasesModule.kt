package com.mobileprism.fishing.di

import com.mobileprism.fishing.ui.use_cases.DeleteUserCatchUseCase
import com.mobileprism.fishing.domain.use_cases.GetMapMarkerByIdUseCase
import com.mobileprism.fishing.domain.use_cases.SavePhotosUseCase
import com.mobileprism.fishing.ui.use_cases.UpdateUserCatchUseCase
import com.mobileprism.fishing.ui.use_cases.*
import com.mobileprism.fishing.ui.use_cases.notes.DeleteUserMarkerNoteUseCase
import com.mobileprism.fishing.ui.use_cases.notes.SaveUserMarkerNoteUseCase
import org.koin.dsl.module

val useCasesModule = module {

    factory { SaveUserMarkerNoteUseCase(get()) }
    factory { DeleteUserMarkerNoteUseCase(get()) }

    factory { GetUserCatchesUseCase(get()) }
    factory { GetNewCatchWeatherUseCase(get(), get()) }
    factory { SaveNewCatchUseCase(get(), get(), get()) }
    factory { GetUserPlacesUseCase(get()) }
    factory { GetUserPlacesListUseCase(get()) }
    factory { AddNewPlaceUseCase(get()) }
    factory { GetFishActivityUseCase(get()) }
    factory { GetFreeWeatherUseCase(get()) }
    factory { DeleteUserCatchUseCase(get(), get()) }
    factory { GetMapMarkerByIdUseCase(get()) }
    factory { SavePhotosUseCase(get()) }
    factory { UpdateUserCatchUseCase(get(), get()) }
    //factory { SubscribeOnUserCatchStateUseCase(get()) }
}

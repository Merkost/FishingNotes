package com.mobileprism.fishing.di

import com.mobileprism.fishing.domain.use_cases.GetFishActivityUseCase
import com.mobileprism.fishing.domain.use_cases.GetFreeWeatherUseCase
import com.mobileprism.fishing.domain.use_cases.SavePhotosUseCase
import com.mobileprism.fishing.domain.use_cases.SubscribeOnUserCatchStateUseCase
import com.mobileprism.fishing.domain.use_cases.catches.DeleteUserCatchUseCase
import com.mobileprism.fishing.domain.use_cases.catches.GetCatchStatisticsUseCase
import com.mobileprism.fishing.domain.use_cases.catches.GetNewCatchWeatherUseCase
import com.mobileprism.fishing.domain.use_cases.catches.GetUserCatchesUseCase
import com.mobileprism.fishing.domain.use_cases.catches.UpdateUserCatchUseCase
import com.mobileprism.fishing.domain.use_cases.notes.DeleteUserMarkerNoteUseCase
import com.mobileprism.fishing.domain.use_cases.notes.SaveUserMarkerNoteUseCase
import com.mobileprism.fishing.domain.use_cases.places.AddNewPlaceUseCase
import com.mobileprism.fishing.domain.use_cases.places.GetMapMarkerByIdUseCase
import com.mobileprism.fishing.domain.use_cases.places.GetUserPlacesListUseCase
import org.koin.dsl.module

val commonUseCasesModule = module {
    factory { SaveUserMarkerNoteUseCase(get()) }
    factory { DeleteUserMarkerNoteUseCase(get()) }
    factory { GetUserCatchesUseCase(get()) }
    factory { GetUserPlacesListUseCase(get()) }
    factory { GetFishActivityUseCase(get()) }
    factory { GetFreeWeatherUseCase(get()) }
    factory { DeleteUserCatchUseCase(get(), get()) }
    factory { GetMapMarkerByIdUseCase(get()) }
    factory { SubscribeOnUserCatchStateUseCase(get()) }
    factory { GetCatchStatisticsUseCase(get()) }
    factory { GetNewCatchWeatherUseCase(get(), get()) }
    factory { SavePhotosUseCase(get()) }
    factory { UpdateUserCatchUseCase(get(), get()) }
    factory { AddNewPlaceUseCase(get(), get()) }
}

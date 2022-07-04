package com.mobileprism.fishing.di

import com.mobileprism.fishing.domain.use_cases.*
import com.mobileprism.fishing.domain.use_cases.catches.*
import com.mobileprism.fishing.domain.use_cases.notes.DeleteUserMarkerNoteUseCase
import com.mobileprism.fishing.domain.use_cases.notes.SaveUserMarkerNoteUseCase
import com.mobileprism.fishing.domain.use_cases.places.AddNewPlaceUseCase
import com.mobileprism.fishing.domain.use_cases.places.GetMapMarkerByIdUseCase
import com.mobileprism.fishing.domain.use_cases.places.GetUserPlacesListUseCase
import com.mobileprism.fishing.domain.use_cases.users.*
import org.koin.dsl.module

val useCasesModule = module {

    factory { SaveUserMarkerNoteUseCase(get()) }
    factory { DeleteUserMarkerNoteUseCase(get()) }

    factory { GetUserCatchesUseCase(get()) }
    factory { GetNewCatchWeatherUseCase(get(), get()) }
    factory { SaveNewCatchUseCase(get(), get(), get()) }
    factory { GetUserPlacesListUseCase(get()) }
    factory { AddNewPlaceUseCase(get()) }
    factory { GetFishActivityUseCase(get()) }
    factory { GetFreeWeatherUseCase(get()) }
    factory { DeleteUserCatchUseCase(get(), get()) }
    factory { GetMapMarkerByIdUseCase(get()) }
    factory { SavePhotosUseCase(get()) }
    factory { UpdateUserCatchUseCase(get(), get()) }
    factory { SubscribeOnUserCatchStateUseCase(get()) }
    factory { GetPlaceNameUseCase(get()) }
    factory { GetUserCatchesByMarkerId(get()) }
    factory { RegisterNewUserUseCase(get()) }
    factory { SignInUserUserCase(get()) }
    factory { SignInUserWithGoogleUseCase(get()) }
    factory { SignOutCurrentUserUserCase(get()) }
    factory { SubscribeOnCurrentUserUseCase(get()) }
    factory { SkipAuthorizationUseCase(get()) }
}

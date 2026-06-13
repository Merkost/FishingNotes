package com.mobileprism.fishing.di

import com.mobileprism.fishing.domain.use_cases.PlaceNameResolver
import com.mobileprism.fishing.ui.viewmodels.LoginViewModel
import com.mobileprism.fishing.ui.viewmodels.NewCatchMasterViewModel
import com.mobileprism.fishing.ui.viewmodels.StatisticsViewModel
import com.mobileprism.fishing.ui.viewmodels.UserCatchViewModel
import com.mobileprism.fishing.ui.viewmodels.UserCatchesViewModel
import com.mobileprism.fishing.ui.viewmodels.UserPlaceViewModel
import com.mobileprism.fishing.ui.viewmodels.UserPlacesViewModel
import com.mobileprism.fishing.ui.viewmodels.UserViewModel
import com.mobileprism.fishing.ui.viewmodels.WeatherViewModel
import com.mobileprism.fishing.viewmodels.EditProfileViewModel
import com.mobileprism.fishing.viewmodels.MainViewModel
import com.mobileprism.fishing.viewmodels.MapViewModel
import com.mobileprism.fishing.viewmodels.OnboardingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val commonViewModelsModule = module {
    viewModel { StatisticsViewModel(getStatisticsUseCase = get()) }
    viewModel { EditProfileViewModel(userDatastore = get(), userRepository = get(), savePhotos = get()) }
    viewModel {
        UserViewModel(
            userRepository = get(),
            userDatastore = get(),
            repository = get(),
            getUserCatchUseCase = get()
        )
    }
    viewModel { MainViewModel(repository = get(), syncStatusProvider = get(), userPreferences = get()) }
    viewModel { OnboardingViewModel(userPreferences = get()) }
    viewModel {
        LoginViewModel(
            repository = get(),
            analyticsTracker = get()
        )
    }
    viewModel {
        MapViewModel(
            getUserPlacesListUseCase = get(),
            addNewPlaceUseCase = get(),
            getFreeWeatherUseCase = get(),
            getFishActivityUseCase = get(),
            getPlaceNameUseCase = get<PlaceNameResolver>(),
            userPreferences = get(),
            locationManager = get(),
        )
    }
    viewModel { parameters ->
        UserCatchViewModel(
            userCatch = parameters.get(),
            updateUserCatch = get(),
            deleteUserCatch = get(),
            getMapMarkerById = get(),
            subscribeOnUserCatchState = get()
        )
    }
    viewModel {
        WeatherViewModel(
            weatherRepository = get(),
            repository = get()
        )
    }
    viewModel {
        UserPlaceViewModel(
            markersRepo = get(),
            catchesRepo = get<com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryRead>(),
            saveNewUserMarkerNoteUseCase = get(),
            deleteUserMarkerNoteUseCase = get()
        )
    }
    viewModel { UserCatchesViewModel(userCatchesUseCase = get(), repository = get()) }
    viewModel { UserPlacesViewModel(repository = get()) }
    viewModel { parameters ->
        NewCatchMasterViewModel(
            placeState = parameters.get(),
            getNewCatchWeatherUseCase = get(),
            saveNewCatchUseCase = get(),
            getUserPlacesListUseCase = get(),
            getFishSpeciesHistoryUseCase = get()
        )
    }
}

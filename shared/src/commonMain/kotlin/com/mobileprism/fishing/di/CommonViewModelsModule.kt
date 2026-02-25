package com.mobileprism.fishing.di

import com.mobileprism.fishing.ui.viewmodels.StatisticsViewModel
import com.mobileprism.fishing.ui.viewmodels.UserViewModel
import com.mobileprism.fishing.viewmodels.EditProfileViewModel
import com.mobileprism.fishing.viewmodels.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val commonViewModelsModule = module {
    viewModel { StatisticsViewModel(getStatisticsUseCase = get()) }
    viewModel { EditProfileViewModel(userDatastore = get(), userRepository = get()) }
    viewModel {
        UserViewModel(
            userRepository = get(),
            userDatastore = get(),
            repository = get(),
            getUserCatchUseCase = get()
        )
    }
    viewModel { MainViewModel(repository = get(), syncStatusProvider = get()) }
}

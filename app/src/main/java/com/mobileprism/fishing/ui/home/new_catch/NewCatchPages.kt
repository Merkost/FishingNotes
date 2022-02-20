package com.mobileprism.fishing.ui.home.new_catch

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavController
import com.mobileprism.fishing.ui.home.new_catch.pages.*
import com.mobileprism.fishing.domain.NewCatchMasterViewModel

typealias NewCatchScreenItem = @Composable (viewModel: NewCatchMasterViewModel, navController: NavController) -> Unit

sealed class NewCatchPage(var screen: NewCatchScreenItem) {
    @ExperimentalComposeUiApi
    class NewCatchPlacePage() : NewCatchPage(screen = { viewModel, navController ->
        NewCatchPlace(viewModel, navController)
    })

    class NewCatchFishInfoPage() : NewCatchPage(screen = { viewModel, navController ->
        NewCatchFishInfo(viewModel, navController)
    })

    class NewCatchWayOfFishingPage() : NewCatchPage(screen = { viewModel, navController ->
        NewCatchNote(viewModel, navController)
    })

    @ExperimentalComposeUiApi
    class NewCatchWeatherPage() : NewCatchPage(screen = { viewModel, navController ->
        NewCatchWeather(viewModel, navController)
    })

    @ExperimentalAnimationApi
    @ExperimentalComposeUiApi
    class NewCatchPhotosPage() : NewCatchPage(screen = { viewModel, navController ->
        NewCatchPhotos(viewModel, navController)
    })
}


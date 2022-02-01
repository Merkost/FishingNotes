package com.mobileprism.fishing.compose.ui.home.new_catch

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import com.mobileprism.fishing.domain.NewCatchMasterViewModel

typealias NewCatchScreenItem = @Composable (viewModel: NewCatchMasterViewModel, navController: NavController) -> Unit

sealed class NewCatchPage(var screen: NewCatchScreenItem) {
    class NewCatchPlacePage() : NewCatchPage(screen = { viewModel, navController ->
        NewCatchPlace(viewModel, navController)
    })

    class NewCatchFishInfoPage() : NewCatchPage(screen = { viewModel, navController ->
        NewCatchFishInfo(viewModel, navController)
    })

    class NewCatchWayOfFishingPage() : NewCatchPage(screen = { viewModel, navController ->
        NewCatchWayOfFishing(viewModel, navController)
    })

    class NewCatchWeatherPage() : NewCatchPage(screen = { viewModel, navController ->
        NewCatchWeather(viewModel, navController)
    })

    class NewCatchPhotosPage() : NewCatchPage(screen = { viewModel, navController ->
        NewCatchPhotos(viewModel, navController)
    })
}

@Composable
fun NewCatchPlace(viewModel: NewCatchMasterViewModel, navController: NavController) {
    val place = viewModel.currentPlace.collectAsState()
    Text(text = place.value?.title ?: "No place")
}

@Composable
fun NewCatchFishInfo(viewModel: NewCatchMasterViewModel, navController: NavController) {
    Text(text = "Fish")
}

@Composable
fun NewCatchWayOfFishing(viewModel: NewCatchMasterViewModel, navController: NavController) {
    Text(text = "WayOfFishing")
}

@Composable
fun NewCatchWeather(viewModel: NewCatchMasterViewModel, navController: NavController) {
    Text(text = "Weather")
}

@Composable
fun NewCatchPhotos(viewModel: NewCatchMasterViewModel, navController: NavController) {
    Text(text = "Photos")
}
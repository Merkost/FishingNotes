package com.mobileprism.fishing.compose.ui.home.new_catch

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.mobileprism.fishing.domain.NewCatchMasterViewModel

typealias NewCatchScreenItem = @Composable (viewModel: NewCatchMasterViewModel) -> Unit

sealed class NewCatchPage(var screen: NewCatchScreenItem) {
    class NewCatchPlacePage() : NewCatchPage(screen = { NewCatchPlace(it) })
    class NewCatchFishInfoPage() : NewCatchPage(screen = { NewCatchFishInfo(it) })
    class NewCatchWayOfFishingPage() : NewCatchPage(screen = { NewCatchWayOfFishing(it) })
    class NewCatchWeatherPage() : NewCatchPage(screen = { NewCatchWeather(it) })
    class NewCatchPhotosPage() : NewCatchPage(screen = { NewCatchPhotos(it) })
}

@Composable
fun NewCatchPlace(viewModel: NewCatchMasterViewModel) {
    Text(text = "Place")
}

@Composable
fun NewCatchFishInfo(viewModel: NewCatchMasterViewModel) {
    Text(text = "Fish")
}

@Composable
fun NewCatchWayOfFishing(viewModel: NewCatchMasterViewModel) {
    Text(text = "WayOfFishing")
}

@Composable
fun NewCatchWeather(viewModel: NewCatchMasterViewModel) {
    Text(text = "Weather")
}

@Composable
fun NewCatchPhotos(viewModel: NewCatchMasterViewModel) {
    Text(text = "Photos")
}
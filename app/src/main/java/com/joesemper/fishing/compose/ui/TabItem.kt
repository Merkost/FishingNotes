package com.joesemper.fishing.compose.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.user_catches.UserCatchesLoading
import com.joesemper.fishing.compose.ui.home.user_catches.UserCatchesScreen
import com.joesemper.fishing.compose.ui.home.user_places.UserPlaces
import com.joesemper.fishing.compose.ui.home.user_places.UserPlacesLoading
import com.joesemper.fishing.compose.ui.home.user_places.UserPlacesScreen

typealias ComposableFun = @Composable () -> Unit

sealed class TabItem(var icon: Int, var title: String, var screen: ComposableFun) {

    @ExperimentalAnimationApi
    object Places : TabItem(R.drawable.ic_baseline_location_on_24, "Places", { UserPlacesScreen() })

    @ExperimentalAnimationApi
    object Catches : TabItem(R.drawable.ic_add_catch, "Catches", { UserCatchesScreen() })

}

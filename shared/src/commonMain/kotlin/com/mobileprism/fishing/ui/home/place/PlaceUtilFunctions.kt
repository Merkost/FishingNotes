package com.mobileprism.fishing.ui.home.place

import androidx.navigation.NavController
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.ui.MainDestinations

fun newCatchClicked(navController: NavController, place: UserMapMarker) {
    navController.navigate(MainDestinations.NewCatch(place = place))
}

expect fun openMapNavigation(marker: UserMapMarker, analyticsTracker: AnalyticsTracker)

expect fun shareMarkerLocation(marker: UserMapMarker, analyticsTracker: AnalyticsTracker)

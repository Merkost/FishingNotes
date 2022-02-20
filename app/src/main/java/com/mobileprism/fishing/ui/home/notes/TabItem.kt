package com.mobileprism.fishing.ui.home.notes

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.mobileprism.fishing.R

typealias ComposableFun = @Composable (navController: NavController) -> Unit

sealed class TabItem(var icon: Int, var titleRes: Int, var screen: ComposableFun) {

    @ExperimentalAnimationApi
    object Places :
        TabItem(R.drawable.ic_baseline_location_on_24, R.string.places, { navController ->
            UserPlacesScreen(navController = navController)
        })

    @ExperimentalFoundationApi
    @ExperimentalAnimationApi
    object Catches : TabItem(R.drawable.ic_fish, R.string.catches, { navController ->
        UserCatchesScreen(navController = navController)
    })

    @ExperimentalFoundationApi
    @ExperimentalAnimationApi
    object PlaceCatches : TabItem(R.drawable.ic_fish, R.string.catches, { _ -> })

    @ExperimentalFoundationApi
    @ExperimentalAnimationApi
    object Note : TabItem(R.drawable.ic_baseline_sticky_note_2_24, R.string.note, { _ -> })

//    class ForADay(weatherForecast: WeatherForecast) :
//        TabItem(R.drawable.ic_baseline_today_24, R.string.for_today, { navController ->
//            WeatherForADay(weather = weatherForecast)
//        })
//
//    class ForAWeek(weatherForecast: WeatherForecast) :
//        TabItem(R.drawable.calendar_week, R.string.for_a_week, { navController ->
//            WeatherForAWeek(weather = weatherForecast)
//        })
}

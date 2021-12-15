package com.joesemper.fishing.compose.ui.home.notes

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.joesemper.fishing.R

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

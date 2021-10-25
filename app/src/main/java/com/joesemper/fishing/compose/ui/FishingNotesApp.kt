package com.joesemper.fishing.compose.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.joesemper.fishing.compose.ui.home.*
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.theme.FigmaTheme
import kotlinx.coroutines.InternalCoroutinesApi

@ExperimentalPermissionsApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalMaterialApi
@InternalCoroutinesApi
@Composable
fun FishingNotesApp() {
    ProvideWindowInsets {
        FigmaTheme {
            val appStateHolder = rememberAppStateHolder()
            val bottomBarState = remember { mutableStateOf(true) }
            var visibility = remember { true }
            Scaffold (
                bottomBar = {
                    var lastRoute: String = "home/map"
                    if (appStateHolder.shouldShowBottomBar/* && bottomBarState.value*/) {
                        visibility = true
                        lastRoute = appStateHolder.currentRoute!!
                    }
                    else visibility = false
                    AnimatedVisibility(visible = visibility) {
                        FishingNotesBottomBar(
                            tabs = appStateHolder.bottomBarTabs,
                            currentRoute = lastRoute,
                            navigateToRoute = appStateHolder::navigateToBottomBarRoute
                        )
                    }
                },
                /*snackbarHost = {
                    SnackbarHost(
                        hostState = it,
                        modifier = Modifier.systemBarsPadding(),
                        snackbar = { snackbarData -> Snackbar(snackbarData) }
                    )
                },*/
                scaffoldState = appStateHolder.scaffoldState,
                /*modifier = if (appStateHolder.currentRoute == HomeSections.MAP.route)
                    Modifier.statusBarsHeight()
                else Modifier*/
            ) { innerPaddingModifier ->
                Column /*Surface*/ {
                    //Spacer(modifier = Modifier.statusBarsHeight())
                    NavHost(
                        navController = appStateHolder.navController,
                        startDestination = MainDestinations.MAP_ROUTE,
                        modifier = /*if (appStateHolder.currentRoute != HomeSections.MAP.route)*/
                            Modifier.padding(innerPaddingModifier) /*else Modifier*/
                    ) {
                        NavGraph(
                            navController = appStateHolder.navController,
                            onSnackSelected = appStateHolder::navigateToSnackDetail,
                            upPress = appStateHolder::upPress,
                            bottomBarState = bottomBarState
                        )
                    }
                }




            }
        }
    }
}

@ExperimentalPermissionsApi
@ExperimentalCoilApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@InternalCoroutinesApi
@ExperimentalMaterialApi
private fun NavGraphBuilder.NavGraph(
    onSnackSelected: (Long, NavBackStackEntry) -> Unit,
    upPress: () -> Unit,
    navController: NavController,
    bottomBarState: MutableState<Boolean>

) {
    navigation(
        route = MainDestinations.MAP_ROUTE,
        startDestination = HomeSections.MAP.route
    ) {
        addHomeGraph(onSnackSelected, navController, Modifier, bottomBarState)
    }
    /*composable(
        "${MainDestinations.SNACK_DETAIL_ROUTE}/{${MainDestinations.SNACK_ID_KEY}}",
        arguments = listOf(navArgument(MainDestinations.SNACK_ID_KEY) { type = NavType.LongType })
    ) { backStackEntry ->
        val arguments = requireNotNull(backStackEntry.arguments)
        val snackId = arguments.getLong(MainDestinations.SNACK_ID_KEY)
//        SnackDetail(snackId, upPress)
    }*/
    composable(
        route = MainDestinations.NEW_CATCH_ROUTE,
        //arguments = listOf(navArgument(MainDestinations.SNACK_ID_KEY) { type = NavType.LongType })
        //arguments = listOf(navArgument(Arguments.PLACE) {type = NavType.ParcelableArrayType(UserMapMarker::class.java)})
    ) { backStackEntry ->
        val place = navController.previousBackStackEntry?.arguments?.getParcelable<UserMapMarker>(Arguments.PLACE)
        NewCatchScreen(navController, place)
        //val arguments = requireNotNull(backStackEntry.arguments)
        //val snackId = arguments.getLong(MainDestinations.SNACK_ID_KEY)
        //        SnackDetail(snackId, upPress)
    }
    composable(
        route = MainDestinations.PLACE_ROUTE,
    ) {
        val marker = navController.previousBackStackEntry?.arguments?.getParcelable<UserMapMarker>(Arguments.PLACE)
        UserPlaceScreen(navController, marker)
    }
    composable(
        route = MainDestinations.CATCH_ROUTE,
    ) {
        val catch = navController.previousBackStackEntry?.arguments?.getParcelable<UserCatch>(Arguments.CATCH)
        AnimatedVisibility(true,
            enter= slideInVertically(animationSpec = tween(800)),) {
            UserCatchScreen(navController, catch) }
    }
}
package com.joesemper.fishing.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.*
import com.joesemper.fishing.compose.ui.rememberAppStateHolder
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
            Scaffold (
                bottomBar = {
                    if (appStateHolder.shouldShowBottomBar) {
                        FishingNotesBottomBar(
                            tabs = appStateHolder.bottomBarTabs,
                            currentRoute = appStateHolder.currentRoute!!,
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
//                modifier = Modifier.padding(top = Modifier.statusBarsHeight() as Dp)
            ) { innerPaddingModifier ->
                Column {
                    Spacer(modifier = Modifier.statusBarsHeight())
                    NavHost(
                        navController = appStateHolder.navController,
                        startDestination = MainDestinations.MAP_ROUTE,
                        modifier = Modifier.padding(innerPaddingModifier)
                    ) {
                        NavGraph(
                            navController = appStateHolder.navController,
                            onSnackSelected = appStateHolder::navigateToSnackDetail,
                            upPress = appStateHolder::upPress
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
    navController: NavController
) {
    navigation(
        route = MainDestinations.MAP_ROUTE,
        startDestination = HomeSections.MAP.route
    ) {
        addHomeGraph(onSnackSelected, navController)
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
        UserCatchScreen(navController, catch)
    }
}
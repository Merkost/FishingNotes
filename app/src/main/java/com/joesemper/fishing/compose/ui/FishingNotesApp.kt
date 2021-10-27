package com.joesemper.fishing.compose.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.joesemper.fishing.compose.ui.home.*
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
                    AnimatedVisibility(appStateHolder.shouldShowBottomBar) {
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
                /*modifier = if (appStateHolder.currentRoute == HomeSections.MAP.route)
                    Modifier.statusBarsHeight()
                else Modifier*/
            ) { innerPaddingModifier ->
                Column /*Surface*/ {
                    //Spacer(modifier = Modifier.statusBarsHeight())
                    NavHost(
                        navController = appStateHolder.navController,
                        startDestination = MainDestinations.HOME_ROUTE,
                        modifier = /*if (appStateHolder.currentRoute != HomeSections.MAP.route)*/
                            Modifier.padding(innerPaddingModifier) /*else Modifier*/
                    ) {
                        NavGraph(
                            navController = appStateHolder.navController,
                            onSnackSelected = appStateHolder::navigateToSnackDetail,
                            upPress = appStateHolder::upPress,
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
) {
    navigation(
        route = MainDestinations.HOME_ROUTE,
        startDestination = HomeSections.MAP.route/*"${HomeSections.MAP.route}?${Arguments.MAP_NEW_PLACE}={${Arguments.MAP_NEW_PLACE}}"*/
    ) {
        addHomeGraph(onSnackSelected, navController)
    }
    composable(
        "${MainDestinations.SNACK_DETAIL_ROUTE}/{${MainDestinations.SNACK_ID_KEY}}",
        arguments = listOf(navArgument(MainDestinations.SNACK_ID_KEY) { type = NavType.LongType })
    ) { backStackEntry ->
        val arguments = requireNotNull(backStackEntry.arguments)
        val snackId = arguments.getLong(MainDestinations.SNACK_ID_KEY)
        //SnackDetail(snackId, upPress)
    }

    /*composable(
        route = "${MainDestinations.MAP_ROUTE}/{${Arguments.MAP_NEW_PLACE}}",
        arguments = listOf(navArgument(Arguments.MAP_NEW_PLACE) { type = NavType.BoolType })
    ) {
        Map() }*/

    composable(
        route = MainDestinations.NEW_CATCH_ROUTE,
    ) {
        NewCatchScreen({
            navController.popBackStack(route = MainDestinations.NEW_CATCH_ROUTE,
                inclusive = true) },
            it.requiredArg(Arguments.PLACE))
    }

    composable(
        route = MainDestinations.PLACE_ROUTE,
    ) { UserPlaceScreen(navController, it.requiredArg(Arguments.PLACE)) }

    composable(
        route = MainDestinations.CATCH_ROUTE,
    ) { UserCatchScreen(navController, it.requiredArg(Arguments.CATCH)) }
}


package com.joesemper.fishing.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.navigation
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.FishingNotesBottomBar
import com.joesemper.fishing.compose.ui.home.HomeSections
import com.joesemper.fishing.compose.ui.home.addHomeGraph
import com.joesemper.fishing.compose.ui.rememberAppStateHolder
import com.joesemper.fishing.ui.theme.FigmaTheme
import kotlinx.coroutines.InternalCoroutinesApi

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
                snackbarHost = {
                    SnackbarHost(
                        hostState = it,
                        modifier = Modifier.systemBarsPadding(),
                        snackbar = { snackbarData -> Snackbar(snackbarData) }
                    )
                },
                scaffoldState = appStateHolder.scaffoldState
            ) { innerPaddingModifier ->
                NavHost(
                    navController = appStateHolder.navController,
                    startDestination = MainDestinations.MAP_ROUTE,
                    modifier = Modifier.padding(innerPaddingModifier)
                ) {
                    fishingNotesNavGraph(
                        onSnackSelected = appStateHolder::navigateToSnackDetail,
                        upPress = appStateHolder::upPress
                    )
                }
            }
        }
    }
}

@ExperimentalCoilApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@InternalCoroutinesApi
@ExperimentalMaterialApi
private fun NavGraphBuilder.fishingNotesNavGraph(
    onSnackSelected: (Long, NavBackStackEntry) -> Unit,
    upPress: () -> Unit
) {
    navigation(
        route = MainDestinations.MAP_ROUTE,
        startDestination = HomeSections.MAP.route
    ) {
        addHomeGraph(onSnackSelected)
    }
    composable(
        "${MainDestinations.SNACK_DETAIL_ROUTE}/{${MainDestinations.SNACK_ID_KEY}}",
        arguments = listOf(navArgument(MainDestinations.SNACK_ID_KEY) { type = NavType.LongType })
    ) { backStackEntry ->
        val arguments = requireNotNull(backStackEntry.arguments)
        val snackId = arguments.getLong(MainDestinations.SNACK_ID_KEY)
//        SnackDetail(snackId, upPress)
    }
}
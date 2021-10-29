package com.joesemper.fishing.compose.ui

import android.view.animation.OvershootInterpolator
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.*
import com.joesemper.fishing.domain.UserCatchViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.ui.MainActivity
import com.joesemper.fishing.ui.theme.FigmaTheme
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

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
            var visible by remember { mutableStateOf(false) }

            Scaffold(
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
                        snackbar = { snackbarData -> AppSnackbar(snackbarData) }
                    )
                },
                scaffoldState = appStateHolder.scaffoldState,
                /*modifier = if (appStateHolder.currentRoute == HomeSections.MAP.route)
                    Modifier.statusBarsHeight()
                else Modifier*/
            ) { innerPaddingModifier ->
                Column () {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(
                            durationMillis = MainActivity.splashFadeDurationMillis))
                        /*slideInVertically(
                            initialOffsetY = {
                                // Slide in from top
                                -it
                            },
                            animationSpec = tween(
                                durationMillis = MainActivity.splashFadeDurationMillis,
                                easing = CubicBezierEasing(0f, 0f, 0f, 1f)

                            )
                        )*/,
                    ) {
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
                LaunchedEffect(true) {
                    visible = true
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
        startDestination = HomeSections.MAP.route
    ) {
        addHomeGraph(onSnackSelected, navController)
    }

    composable(MainDestinations.LOGIN_ROUTE) {
        LoginScreen(navController = navController)
    }

    composable(
        route = MainDestinations.NEW_CATCH_ROUTE,
    ) {
        NewCatchScreen({ navController.popBackStack(route = MainDestinations.NEW_CATCH_ROUTE,
                inclusive = true) }, it.requiredArg(Arguments.PLACE))
    }

    composable(
        route = MainDestinations.PLACE_ROUTE,
    ) { UserPlaceScreen(navController, it.requiredArg(Arguments.PLACE)) }

    composable(
        route = MainDestinations.CATCH_ROUTE,
    ) { UserCatchScreen(navController, it.requiredArg(Arguments.CATCH)) }
}



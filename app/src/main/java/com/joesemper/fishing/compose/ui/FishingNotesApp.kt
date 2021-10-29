package com.joesemper.fishing.compose.ui

import android.view.animation.OvershootInterpolator
import androidx.annotation.StringRes
import androidx.compose.animation.*
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
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
import com.joesemper.fishing.domain.SplashViewModel
import com.joesemper.fishing.domain.UserCatchViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.common.User
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
                        snackbar = { snackbarData -> AppSnackbar(snackbarData) }
                    )
                },
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
        startDestination = HomeSections.MAP.route
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

    composable(
        route = MainDestinations.NEW_CATCH_ROUTE,
    ) {
        NewCatchScreen({ navController.popBackStack(route = MainDestinations.NEW_CATCH_ROUTE,
                inclusive = true) }, it.requiredArg(Arguments.PLACE))
    }

    /*composable(
        route = MainDestinations.MAP_TO_NEW_CATCH_ROUTE,
        ) {
        NewCatchScreen({
            navController.popBackStack(route = MainDestinations.MAP_TO_NEW_CATCH_ROUTE,
                inclusive = true) },
            it.requiredArg(Arguments.PLACE))
    }*/

    composable(
        route = MainDestinations.PLACE_ROUTE,
    ) { UserPlaceScreen(navController, it.requiredArg(Arguments.PLACE)) }

    /*composable(
        route = MainDestinations.MAP_TO_PLACE_ROUTE,
    ) { UserPlaceScreen(navController, it.requiredArg(Arguments.PLACE)) }*/

    composable(
        route = MainDestinations.CATCH_ROUTE,
    ) { UserCatchScreen(navController, it.requiredArg(Arguments.CATCH)) }
}

@Composable
fun LoginScreen(navController: NavController) {

}

@Composable
fun SplashScreen(navController: NavController) {
    val viewModel: SplashViewModel = get()
    val userState = viewModel.subscribe().collectAsState()

    LaunchedEffect(userState) {
        when (userState.value) {
            is BaseViewState.Success<*> -> onSuccess((userState.value as BaseViewState.Success<*>).data as User?, navController)
            is BaseViewState.Loading -> { }
            is BaseViewState.Error -> { } //showErrorSnackbar
        }
    }

    val scale = remember {
        androidx.compose.animation.core.Animatable(0f)
    }

    // AnimationEffect
    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 0.2f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = {
                    OvershootInterpolator(4f).getInterpolation(it)
                })
        )
        delay(1000)
        navController.navigate("main_screen")
    }

    // Image
    Box(contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()) {
        Image(painter = painterResource(id = R.drawable.ic_fishing),
            contentDescription = "Logo",
            modifier = Modifier.scale(scale.value))
    }
}

private fun onSuccess(user: User?, navController: NavController) {
    if (user != null) {
        navController.navigate("main_screen")
    } else {
        navController.navigate("login_screen")
    }
}


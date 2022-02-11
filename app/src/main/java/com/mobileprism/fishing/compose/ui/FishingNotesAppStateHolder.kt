package com.mobileprism.fishing.compose.ui

import android.content.res.Resources
import android.os.Parcelable
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.navigation.*
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobileprism.fishing.compose.ui.home.HomeSections
import com.mobileprism.fishing.compose.ui.home.SnackbarManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Destinations used in the [FishingNotesApp].
 */
object MainDestinations {

    const val LOGIN_ROUTE = "login"
    const val HOME_ROUTE = "home"
    const val SETTINGS = "settings"
    const val ABOUT_APP = "about_app"

    const val NEW_CATCH_ROUTE = "new_catch"
    const val PLACE_ROUTE = "place"

    const val CATCH_ROUTE = "catch"
    const val DAILY_WEATHER_ROUTE = "daily_weather"

    const val MAP_ROUTE = "map"
    const val WEATHER_ROUTE = "weather"

    const val NOTES_ROUTE = "notes"
    const val NOTES_TO_NEW_CATCH_ROUTE = "${HOME_ROUTE}/${NOTES_ROUTE}/${NEW_CATCH_ROUTE}"
    const val NOTES_TO_PLACE_ROUTE = "${HOME_ROUTE}/${NOTES_ROUTE}/${PLACE_ROUTE}"

}

object Arguments {
    const val PLACE = "place_arg"
    const val CATCH = "catch_arg"
    const val WEATHER_DATA = "daily_weather_data_arg"
    const val WEATHER_INDEX = "daily_weather_index_arg"
    const val MAP_NEW_PLACE = "map_new_place_arg"
}

/**
 * Remembers and creates an instance of [AppStateHolder]
 */
@Composable
fun rememberAppStateHolder(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    navController: NavHostController = rememberNavController(),
    snackbarManager: SnackbarManager = SnackbarManager,
    resources: Resources = resources(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) =
    remember(scaffoldState, navController, snackbarManager, resources, coroutineScope) {
        AppStateHolder(scaffoldState, navController, snackbarManager, resources, coroutineScope)
    }

/**
 * Responsible for holding state related to [FishingNotesApp] and containing UI-related logic.
 */
@Stable
class AppStateHolder(
    val scaffoldState: ScaffoldState,
    val navController: NavHostController,
    private val snackbarManager: SnackbarManager,
    private val resources: Resources,
    coroutineScope: CoroutineScope
) {

    // Process snackbars coming from SnackbarManager
    init {
        coroutineScope.launch {
            snackbarManager.messages.collect { currentMessages ->
                if (currentMessages.isNotEmpty()) {
                    val message = currentMessages[0]
                    val text = resources.getText(message.messageId)
                    val snackbarAction = message.snackbarAction

                    // Display the snackbar on the screen. `showSnackbar` is a function
                    // that suspends until the snackbar disappears from the screen
                    snackbarAction?.let {
                        val actionText = resources.getText(snackbarAction.textId)
                        val result = scaffoldState.snackbarHostState.showSnackbar(
                            text.toString(),
                            actionLabel = actionText.toString().uppercase(),
                            duration = message.duration
                        )
                        when (result) {
                            SnackbarResult.ActionPerformed -> snackbarAction.action
                            SnackbarResult.Dismissed -> {}
                        }
                    } ?: run {
                        scaffoldState.snackbarHostState.showSnackbar(text.toString())
                    }

                    // Once the snackbar is gone or dismissed, notify the SnackbarManager
                    snackbarManager.setMessageShown(message.id)
                }
            }
        }
    }

    // ----------------------------------------------------------
    // BottomBar state source of truth
    // ----------------------------------------------------------

    val bottomBarTabs = HomeSections.values()
    private val bottomBarRoutes = bottomBarTabs.map { it.route }

    // Reading this attribute will cause recompositions when the bottom bar needs shown, or not.
    // Not all routes need to show the bottom bar.
    val shouldShowBottomBar: Boolean
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination?.route in bottomBarRoutes

    // ----------------------------------------------------------
    // Navigation state source of truth
    // ----------------------------------------------------------

    val currentRoute: String?
        get() = navController.currentDestination?.route

    fun upPress() {
        navController.navigateUp()
    }

    fun navigateToBottomBarRoute(route: String) {
        if (route != currentRoute) {
            navController.navigate(route) {
                launchSingleTop = true
                restoreState = true
                // Pop up backstack to the first destination and save state. This makes going back
                // to the start destination when pressing back in any other bottom tab.
                popUpTo(findStartDestination(navController.graph).id) {
                    saveState = true
                }
            }
        }
    }
}

fun NavController.navigate(route: String, vararg args: Pair<String, Parcelable>) {
    navigate(route) {
        if (HomeSections.values().map { it.route }.contains(route)) {
            launchSingleTop = true
            restoreState = true
            // Pop up backstack to the first destination and save state. This makes going back
            // to the start destination when pressing back in any other bottom tab.
            popUpTo(findStartDestination(this@navigate.graph).id) {
                saveState = true
            }
        }
    }


    requireNotNull(currentBackStackEntry?.arguments).apply {
        args.forEach { (key: String, arg: Parcelable) ->
            putParcelable(key, arg)
        }
    }
}

inline fun <reified T : Parcelable> NavBackStackEntry.requiredArg(key: String): T {
    return requireNotNull(arguments) { "arguments bundle is null" }.run {
        requireNotNull(getParcelable(key)) { "argument for $key is null" }
    }
}

/**
 * If the lifecycle is not resumed it means this NavBackStackEntry already processed a nav event.
 *
 * This is used to de-duplicate navigation events.
 */
private fun NavBackStackEntry.lifecycleIsResumed() =
    this.lifecycle.currentState == Lifecycle.State.RESUMED

private val NavGraph.startDestination: NavDestination?
    get() = findNode(startDestinationId)

/**
 * Copied from similar function in NavigationUI.kt
 *
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:navigation/navigation-ui/src/main/java/androidx/navigation/ui/NavigationUI.kt
 */
private tailrec fun findStartDestination(graph: NavDestination): NavDestination {
    return if (graph is NavGraph) findStartDestination(graph.startDestination!!) else graph
}
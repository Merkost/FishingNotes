package com.mobileprism.fishing.ui

import android.content.res.Resources
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.*
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.home.HomeSections
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.weather.DailyWeatherData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data object HomeGraph

@Serializable
data object LoginRoute

/**
 * Destinations used in the [FishingNotesApp].
 */
object MainDestinations {

    @Serializable
    data object Login

    @Serializable
    data object Settings

    @Serializable
    data object AboutApp

    @Serializable
    data object EditProfile

    @Serializable
    data class NewCatch(val place: UserMapMarker? = null)

    @Serializable
    data class Place(val marker: UserMapMarker)

    @Serializable
    data class Catch(val catch: UserCatch)

    @Serializable
    data class DailyWeather(val data: DailyWeatherData)

    @Serializable
    data class Map(
        val isAddingNewPlace: Boolean = false,
        val place: UserMapMarker? = null
    )
}

object HomeTabs {
    @Serializable
    data object NotesTab

    @Serializable
    data class WeatherTab(val place: UserMapMarker? = null)

    @Serializable
    data object ProfileTab
}

/**
 * Remembers and creates an instance of [AppStateHolder]
 */
@Composable
fun rememberAppStateHolder(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navController: NavHostController = rememberNavController(),
    snackbarManager: SnackbarManager = SnackbarManager,
    resources: Resources = resources(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) =
    remember(snackbarHostState, navController, snackbarManager, resources, coroutineScope) {
        AppStateHolder(snackbarHostState, navController, snackbarManager, resources, coroutineScope)
    }

/**
 * Responsible for holding state related to [FishingNotesApp] and containing UI-related logic.
 */
@Stable
class AppStateHolder(
    val snackbarHostState: SnackbarHostState,
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
                        val result = snackbarHostState.showSnackbar(
                            message = text.toString(),
                            actionLabel = actionText.toString().uppercase(),
                            duration = message.duration
                        )
                        when (result) {
                            SnackbarResult.ActionPerformed -> snackbarAction.action
                            SnackbarResult.Dismissed -> {}
                        }
                    } ?: run {
                        snackbarHostState.showSnackbar(text.toString())
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

    val bottomBarTabs = HomeSections.entries.toTypedArray()

    // Reading this attribute will cause recompositions when the bottom bar needs shown, or not.
    // Not all routes need to show the bottom bar.
    val shouldShowBottomBar: Boolean
        @Composable get() {
            val destination = navController.currentBackStackEntryAsState().value?.destination
            return destination != null && HomeSections.entries.any { section ->
                section.hasRoute(destination)
            }
        }

    // ----------------------------------------------------------
    // Navigation state source of truth
    // ----------------------------------------------------------

    fun currentSection(): HomeSections? {
        val destination = navController.currentDestination ?: return null
        return HomeSections.entries.firstOrNull { section ->
            section.hasRoute(destination)
        }
    }

    fun upPress() {
        navController.navigateUp()
    }

    fun navigateToBottomBarRoute(section: HomeSections) {
        val currentSection = currentSection()
        if (section != currentSection) {
            val route: Any = when (section) {
                HomeSections.MAP -> MainDestinations.Map()
                HomeSections.NOTES -> HomeTabs.NotesTab
                HomeSections.WEATHER -> HomeTabs.WeatherTab()
                HomeSections.PROFILE -> HomeTabs.ProfileTab
            }
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

/**
 * Copied from similar function in NavigationUI.kt
 *
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:navigation/navigation-ui/src/main/java/androidx/navigation/ui/NavigationUI.kt
 */
private tailrec fun findStartDestination(graph: NavDestination): NavDestination {
    return if (graph is NavGraph) findStartDestination(graph.findNode(graph.startDestinationId)!!) else graph
}

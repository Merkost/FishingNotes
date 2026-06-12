package com.mobileprism.fishing.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.*
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobileprism.fishing.ui.home.HomeSections
import com.mobileprism.fishing.ui.home.SnackbarManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

/**
 * Remembers and creates an instance of [AppStateHolder]
 */
@Composable
fun rememberAppStateHolder(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navController: NavHostController = rememberNavController(),
    snackbarManager: SnackbarManager = SnackbarManager,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) =
    remember(snackbarHostState, navController, snackbarManager, coroutineScope) {
        AppStateHolder(snackbarHostState, navController, snackbarManager, coroutineScope)
    }

/**
 * Responsible for holding state related to [FishingNotesApp] and containing UI-related logic.
 */
@Stable
class AppStateHolder(
    val snackbarHostState: SnackbarHostState,
    val navController: NavHostController,
    private val snackbarManager: SnackbarManager,
    coroutineScope: CoroutineScope
) {

    // Process snackbars coming from SnackbarManager
    init {
        coroutineScope.launch {
            snackbarManager.messages.collect { currentMessages ->
                if (currentMessages.isNotEmpty()) {
                    val message = currentMessages[0]
                    val text = getString(message.messageId)
                    val snackbarAction = message.snackbarAction

                    // Display the snackbar on the screen. `showSnackbar` is a function
                    // that suspends until the snackbar disappears from the screen
                    snackbarAction?.let {
                        val actionText = getString(snackbarAction.textId)
                        val result = snackbarHostState.showSnackbar(
                            message = text,
                            actionLabel = actionText.uppercase(),
                            duration = message.duration
                        )
                        when (result) {
                            SnackbarResult.ActionPerformed -> snackbarAction.action
                            SnackbarResult.Dismissed -> {}
                        }
                    } ?: run {
                        snackbarHostState.showSnackbar(text)
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
private fun findStartDestination(graph: NavDestination): NavDestination {
    var current = graph
    while (current is NavGraph) {
        current = current.findNode(current.startDestinationId) ?: return current
    }
    return current
}

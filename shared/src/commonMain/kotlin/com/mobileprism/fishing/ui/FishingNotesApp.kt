package com.mobileprism.fishing.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.mobileprism.fishing.ui.home.AppSnackbar
import com.mobileprism.fishing.ui.home.FishingNotesBottomBar
import com.mobileprism.fishing.ui.home.HomeSections
import com.mobileprism.fishing.ui.home.views.SyncStatusIndicator
import com.mobileprism.fishing.viewmodels.MainViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FishingNotesApp() {
    val appStateHolder = rememberAppStateHolder()
    val mainViewModel: MainViewModel = koinViewModel()
    val syncState by mainViewModel.syncState.collectAsState()

    Scaffold(
        bottomBar = {
            if (appStateHolder.shouldShowBottomBar) {
                val currentSection = appStateHolder.currentSection() ?: HomeSections.MAP
                FishingNotesBottomBar(
                    modifier = Modifier,
                    tabs = appStateHolder.bottomBarTabs,
                    currentSection = currentSection,
                    navigateToRoute = appStateHolder::navigateToBottomBarRoute
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = appStateHolder.snackbarHostState,
                modifier = Modifier.systemBarsPadding(),
                snackbar = { snackbarData -> AppSnackbar(snackbarData) }
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.statusBars)
    ) { innerPaddingModifier ->
        Column(modifier = Modifier.padding(innerPaddingModifier)) {
            SyncStatusIndicator(syncState = syncState)
            NavHost(
                navController = appStateHolder.navController,
                startDestination = HomeGraph,
            ) {
                AppNavGraph(
                    navController = appStateHolder.navController,
                    upPress = appStateHolder::upPress,
                )
            }
        }
    }
}

package com.mobileprism.fishing.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.ui.home.AppSnackbar
import com.mobileprism.fishing.ui.home.FishingNotesBottomBar
import com.mobileprism.fishing.ui.home.HomeSections
import com.mobileprism.fishing.ui.home.views.SyncStatusIndicator
import com.mobileprism.fishing.ui.onboarding.OnboardingScreen
import com.mobileprism.fishing.ui.utils.LocalAnalytics
import com.mobileprism.fishing.viewmodels.MainViewModel
import com.mobileprism.fishing.viewmodels.OnboardingViewModel
import com.mobileprism.fishing.viewmodels.RoutingDecision
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FishingNotesApp() {
    val analyticsTracker: AnalyticsTracker = koinInject()
    val mainViewModel: MainViewModel = koinViewModel()
    val onboardingViewModel: OnboardingViewModel = koinViewModel()
    val routing by mainViewModel.routing.collectAsState()

    CompositionLocalProvider(LocalAnalytics provides analyticsTracker) {
        AnimatedContent(
            targetState = routing,
            transitionSpec = {
                if (initialState is RoutingDecision.Onboarding && targetState is RoutingDecision.Login) {
                    (slideInVertically { it } + fadeIn(tween(250))) togetherWith
                            fadeOut(tween(150))
                } else {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                }
            },
            label = "root-routing",
        ) { decision ->
            when (decision) {
                RoutingDecision.Splash -> Unit
                RoutingDecision.Onboarding -> OnboardingScreen(
                    onFinished = { onboardingViewModel.completeOnboarding() },
                )
                RoutingDecision.Login -> LoginScreen()
                RoutingDecision.Home -> FishingNotesMainContent()
            }
        }
    }
}

@Composable
private fun FishingNotesMainContent() {
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
        contentWindowInsets = WindowInsets(0)
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

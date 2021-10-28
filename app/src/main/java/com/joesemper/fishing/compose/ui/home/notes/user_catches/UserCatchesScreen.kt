package com.joesemper.fishing.compose.ui.home.notes.user_catches

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.HomeSections
import com.joesemper.fishing.compose.ui.navigate
import com.joesemper.fishing.domain.UserCatchesViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import org.koin.androidx.compose.getViewModel

@ExperimentalAnimationApi
@Composable
fun UserCatchesScreen(navController: NavController, viewModel: UserCatchesViewModel = getViewModel<UserCatchesViewModel>()) {
    Scaffold() {
        val uiState by viewModel.uiState.collectAsState()
        Crossfade(uiState, animationSpec = tween(500)) { animatedUiState ->
            when (animatedUiState) {
                is BaseViewState.Loading ->
                    UserCatchesLoading { onAddNewCatchClick(navController) }
                is BaseViewState.Success<*> -> UserCatches(
                    (uiState as BaseViewState.Success<*>).data as List<UserCatch>,
                    { onAddNewCatchClick(navController) }, { catch -> onCatchItemClick(catch, navController) })
                is BaseViewState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "An error occurred fetching the catches.")
                    }
                }
            }
        }
    }
}

private fun onAddNewCatchClick(navController: NavController) {
    navController.navigate("${MainDestinations.NEW_CATCH_ROUTE}",
            Arguments.PLACE to UserMapMarker())
}

private fun onCatchItemClick(catch: UserCatch, navController: NavController) {
    navController.navigate(MainDestinations.CATCH_ROUTE, Arguments.CATCH to catch)
}
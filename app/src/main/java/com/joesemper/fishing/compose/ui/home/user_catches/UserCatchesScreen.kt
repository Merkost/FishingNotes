package com.joesemper.fishing.compose.ui.home.user_catches

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
import com.joesemper.fishing.domain.UserCatchesViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.content.UserCatch
import org.koin.androidx.compose.getViewModel

@ExperimentalAnimationApi
@Composable
fun UserCatchesScreen(navController: NavController, viewModel: UserCatchesViewModel = getViewModel<UserCatchesViewModel>()) {
    Scaffold() {
        val uiState by viewModel.uiState.collectAsState()
        Crossfade(uiState, animationSpec = tween(500)) { animatedUiState ->
            when (animatedUiState) {
                is BaseViewState.Loading ->
                    UserCatchesLoading { onAddNewCatchClick() }
                is BaseViewState.Success<*> -> UserCatches(
                    (uiState as BaseViewState.Success<*>).data as List<UserCatch>,
                    { navController.navigate("new_catch")
                        onAddNewCatchClick() }, { catch -> onCatchItemClick(catch) })
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

private fun onAddNewCatchClick() {
    /*val action =
        NotesFragmentDirections.actionNotesFragmentToNewCatchDialogFragment(
            UserMapMarker()
        )
    findNavController().navigate(action)*/
}

private fun onCatchItemClick(catch: UserCatch) {
    /*val action =
        NotesFragmentDirections.actionNotesFragmentToUserCatchFragment(catch)
    findNavController().navigate(action)*/
}
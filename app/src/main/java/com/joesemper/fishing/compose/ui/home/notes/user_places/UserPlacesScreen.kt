package com.joesemper.fishing.compose.ui.home.notes.user_places

import android.os.Parcel
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
import com.joesemper.fishing.compose.ui.navigate
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.HomeSections
import com.joesemper.fishing.domain.UserPlacesViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.content.UserMapMarker
import org.koin.androidx.compose.getViewModel

@ExperimentalAnimationApi
@Composable
fun UserPlacesScreen(
    navController: NavController,
    viewModel: UserPlacesViewModel = getViewModel<UserPlacesViewModel>()
) {
    Scaffold() {
        val uiState by viewModel.uiState.collectAsState()
        Crossfade(uiState, animationSpec = tween(500)) { animatedUiState ->
            when (animatedUiState) {
                is BaseViewState.Loading ->
                    UserPlacesLoading { onAddNewPlaceClick(navController) }
                is BaseViewState.Success<*> -> UserPlaces(
                    (animatedUiState as BaseViewState.Success<*>).data as List<UserMapMarker>, {
                        onAddNewPlaceClick(navController)
                    }, { userMarker ->
                        navController.navigate(
                            MainDestinations.PLACE_ROUTE,
                            Arguments.PLACE to userMarker
                        )
                    }
                )
                is BaseViewState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "An error ocurred fetching the catches.")
                    }
                }
            }
        }

    }
}


private fun onAddNewPlaceClick(navController: NavController) {
    val addNewPlace = true
    //navController.currentBackStackEntry?.arguments?.putBoolean(Arguments.MAP_NEW_PLACE, true)
    navController.navigate("${HomeSections.MAP.route}/$addNewPlace")
}

private fun onPlaceItemClick(place: UserMapMarker) {
    /*val action =
        NotesFragmentDirections.actionNotesFragmentToUserPlaceFragment(place)
    findNavController().navigate(action)*/
}

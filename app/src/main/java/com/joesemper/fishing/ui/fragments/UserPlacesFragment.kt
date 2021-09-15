package com.joesemper.fishing.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.joesemper.fishing.domain.UserPlacesViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.adapters.UserPlacesRVAdapter
import com.joesemper.fishing.ui.composable.user_places.UserPlaces
import com.joesemper.fishing.ui.composable.user_places.UserPlacesLoading
import com.joesemper.fishing.ui.theme.FigmaTheme
import com.joesemper.fishing.utils.showToast
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserPlacesFragment : Fragment() {

    companion object {
        private const val TAG = "PLACES"

        fun newInstance(): Fragment {
            return UserPlacesFragment()
        }
    }

    private val viewModel: UserPlacesViewModel by viewModel()

    private lateinit var adapter: UserPlacesRVAdapter

    @ExperimentalAnimationApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                FigmaTheme {
                    UserPlacesScreen()
                }
            }
        }
    }

    @ExperimentalAnimationApi
    @Composable
    fun UserPlacesScreen() {
        Scaffold() {
            val uiState = viewModel.viewStateFlow.collectAsState()
            Crossfade(uiState, animationSpec = tween(500)) { animatedUiState ->
                when (animatedUiState.value) {
                    is BaseViewState.Loading ->
                        UserPlacesLoading { onAddNewPlaceClick() }
                    is BaseViewState.Success<*> -> UserPlaces(
                        (animatedUiState.value as BaseViewState.Success<*>).data as List<UserMapMarker>, {
                            onAddNewPlaceClick()
                        }, { userMarker ->
                            onPlaceItemClick(userMarker)
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



    private fun onAddNewPlaceClick() {
        showToast(requireContext(), "Not yet implemented")
    }

    private fun onPlaceItemClick(place: UserMapMarker) {
        val action =
            NotesFragmentDirections.actionNotesFragmentToUserPlaceFragment(place)
        findNavController().navigate(action)
    }


}
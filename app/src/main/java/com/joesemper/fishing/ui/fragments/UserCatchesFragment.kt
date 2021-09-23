package com.joesemper.fishing.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.joesemper.fishing.domain.UserCatchesViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.compose.ui.home.user_catches.UserCatches
import com.joesemper.fishing.compose.ui.home.user_catches.UserCatchesLoading
import com.joesemper.fishing.ui.theme.FigmaTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserCatchesFragment : Fragment() {

    companion object {
        private const val TAG = "CATCHES"

        fun newInstance(): Fragment {
            return UserCatchesFragment()
        }
    }

    private val viewModel: UserCatchesViewModel by viewModel()


    @ExperimentalAnimationApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                FigmaTheme {
                    UserCatchesScreen()
                }
            }
        }
    }

    @ExperimentalAnimationApi
    @Composable
    fun UserCatchesScreen() {
        Scaffold() {
            val uiState by viewModel.uiState.collectAsState()
            Crossfade(uiState, animationSpec = tween(500)) { animatedUiState ->
                when (animatedUiState) {
                    is BaseViewState.Loading ->
                        UserCatchesLoading { onAddNewCatchClick() }
                    is BaseViewState.Success<*> -> UserCatches(
                        (uiState as BaseViewState.Success<*>).data as List<UserCatch>,
                        { onAddNewCatchClick() }, { catch -> onCatchItemClick(catch) })
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
        val action =
            NotesFragmentDirections.actionNotesFragmentToNewCatchDialogFragment(
                UserMapMarker()
            )
        findNavController().navigate(action)
    }

    private fun onCatchItemClick(catch: UserCatch) {
        val action =
            NotesFragmentDirections.actionNotesFragmentToUserCatchFragment(catch)
        findNavController().navigate(action)
    }
}
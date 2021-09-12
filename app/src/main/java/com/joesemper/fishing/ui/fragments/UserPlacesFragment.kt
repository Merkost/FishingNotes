package com.joesemper.fishing.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.joesemper.fishing.R
import com.joesemper.fishing.domain.UserPlacesViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.adapters.UserPlacesRVAdapter
import com.joesemper.fishing.ui.theme.FigmaTheme
import com.joesemper.fishing.ui.theme.primaryFigmaColor
import com.joesemper.fishing.ui.theme.secondaryFigmaColor
import kotlinx.coroutines.flow.collect
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class UserPlacesFragment : Fragment(), AndroidScopeComponent {

    companion object {
        private const val TAG = "PLACES"

        fun newInstance(): Fragment {
            return UserPlacesFragment()
        }
    }

    override val scope: Scope by fragmentScope()
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
            when (uiState.value) {
                is BaseViewState.Loading ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                is BaseViewState.Success<*> -> UserPlaces(
                    (uiState.value as BaseViewState.Success<*>).data as List<UserMapMarker>)
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

    @ExperimentalAnimationApi
    @Composable
    fun UserPlaces(
        places: List<UserMapMarker>
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { ItemAddPlace { } }
            items(items = places) {
                ItemPlace(
                    place = it,
                    clickedPlace = { }
                )
            }
        }
    }

    @Composable
    fun MyCard(content: @Composable () -> Unit) {
        Card(elevation = 8.dp, modifier = Modifier.fillMaxWidth().padding(4.dp), content = content)
    }

    @Composable
    fun ItemAddPlace(addPlace: () -> Unit) {
        MyCard {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(110.dp).fillMaxWidth().clickable { addPlace() }
                    .padding(5.dp)
            ) {
                Column(verticalArrangement = Arrangement.Center) {
                    Icon(
                        painterResource(R.drawable.ic_baseline_add_location_24),
                        stringResource(R.string.add_new_place),
                        modifier = Modifier.weight(2f).align(Alignment.CenterHorizontally)
                            .size(50.dp),
                        tint = primaryFigmaColor
                    )
                    Text(stringResource(R.string.add_new_place), modifier = Modifier.weight(1f))
                }
            }
        }

    }

    @ExperimentalAnimationApi
    @Composable
    fun ItemPlace(place: UserMapMarker, clickedPlace: (UserMapMarker) -> Unit) {
        MyCard {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(75.dp).fillMaxWidth().clickable { clickedPlace(place) }
                    .padding(5.dp)
            ) {
                Row(modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(modifier = Modifier.size(50.dp).padding(5.dp)) {
                        Icon(
                            painterResource(R.drawable.ic_baseline_location_on_24),
                            stringResource(R.string.place),
                            modifier = Modifier.padding(5.dp).fillMaxSize(),
                            tint = secondaryFigmaColor
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(place.title)
                        Text(if (place.description.isNullOrEmpty()) "Нет описания" else place.description)
                    }
                }
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painterResource(R.drawable.ic_fish),
                        stringResource(R.string.fish_catch),
                        modifier = Modifier.padding(2.dp)
                    )
                    Text("1")
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeOnViewModel()
    }

    private fun subscribeOnViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.subscribe().collect { viewState ->
                when (viewState) {
                    /*is BaseViewState.Loading -> onLoading()
                    is BaseViewState.Success<*> -> onSuccess(viewState.data as List<UserMapMarker>)
                    is BaseViewState.Error -> onError(viewState.error)*/
                }

            }
        }
    }


}
package com.joesemper.fishing.compose.ui.home.notes

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.navigate
import com.joesemper.fishing.domain.UserCatchesViewModel
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import org.koin.androidx.compose.getViewModel

@ExperimentalAnimationApi
@Composable
fun UserCatchesScreen(
    navController: NavController,
    viewModel: UserCatchesViewModel = getViewModel()
) {
    Scaffold {
        val catches by viewModel.currentContent.collectAsState()
        Crossfade(catches) { animatedUiState ->
            if (animatedUiState != null) {
                UserCatches(
                    catches = animatedUiState,
                    addNewCatchClicked = { onAddNewCatchClick(navController) },
                    userCatchClicked = { catch -> onCatchItemClick(catch, navController) })
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun UserCatches(
    catches: List<UserCatch>,
    addNewCatchClicked: () -> Unit,
    userCatchClicked: (UserCatch) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            ItemAdd(
                icon = painterResource(R.drawable.ic_add_catch),
                text = stringResource(R.string.add_new_catch),
                onClickAction = addNewCatchClicked
            )
        }
        when {
            catches.isNotEmpty() -> {
                items(items = catches.sortedByDescending { it.date }) {
                    ItemUserCatch(
                        userCatch = it,
                        userCatchClicked = userCatchClicked
                    )
                }
            }
            catches.isEmpty() -> {
                item {
                    NoElementsView(
                        mainText = stringResource(R.string.no_cathces_added),
                        secondaryText = stringResource(R.string.add_catch_text),
                        onClickAction = addNewCatchClicked
                    )
                }
            }
        }

    }
}

private fun onAddNewCatchClick(navController: NavController) {
    navController.navigate(MainDestinations.NEW_CATCH_ROUTE, Arguments.PLACE to UserMapMarker())
}

private fun onCatchItemClick(catch: UserCatch, navController: NavController) {
    navController.navigate(MainDestinations.CATCH_ROUTE, Arguments.CATCH to catch)
}
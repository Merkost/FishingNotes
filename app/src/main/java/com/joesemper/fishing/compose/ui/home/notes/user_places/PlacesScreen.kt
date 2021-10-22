package com.joesemper.fishing.compose.ui.home.notes.user_places

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.notes.ItemAdd
import com.joesemper.fishing.compose.ui.home.notes.ItemUserPlace
import com.joesemper.fishing.model.entity.content.UserMapMarker

@ExperimentalAnimationApi
@Composable
fun UserPlaces(
    places: List<UserMapMarker>,
    addNewPlaceClicked: () -> Unit,
    userPlaceClicked: (UserMapMarker) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            ItemAdd(
                icon = painterResource(R.drawable.ic_baseline_add_location_24),
                text = stringResource(R.string.add_new_place),
                onClickAction = addNewPlaceClicked
            )
        }
        items(items = places) { userPlace ->
            ItemUserPlace(
                place = userPlace
            ) { userPlaceClicked(userPlace) }
        }
    }
}
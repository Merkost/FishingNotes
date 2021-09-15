package com.joesemper.fishing.ui.composable.user_places

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.composable.MyCard
import com.joesemper.fishing.ui.theme.primaryFigmaColor
import com.joesemper.fishing.ui.theme.secondaryFigmaColor

@ExperimentalAnimationApi
@Composable
fun UserPlaces(
    places: List<UserMapMarker>,
    addNewPlaceClicked: () -> Unit,
    userPlaceClicked: (UserMapMarker) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { ItemAddPlace { addNewPlaceClicked() } }
        items(items = places) { userPlace ->
            ItemPlace(
                place = userPlace
            ) { userPlaceClicked(userPlace) }
        }
    }
}

@Composable
fun ItemAddPlace(addNewPlace: () -> Unit) {
    MyCard {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(110.dp).fillMaxWidth()
                .clickable { addNewPlace() }
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
fun ItemPlace(place: UserMapMarker, userPlaceClicked: (UserMapMarker) -> Unit) {
    MyCard {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(75.dp).fillMaxWidth()
                .clickable { userPlaceClicked(place) }
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
                    verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxHeight()
                ) {
                    Text(place.title, fontWeight = FontWeight.Bold)
                    if (place.description.isNullOrEmpty()) Text("Нет описания") else Text(place.description!!)
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
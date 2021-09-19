package com.joesemper.fishing.ui.composable.user_places

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.composable.MyCard
import com.joesemper.fishing.ui.theme.primaryFigmaColor
import com.joesemper.fishing.ui.theme.secondaryFigmaColor
import me.vponomarenko.compose.shimmer.shimmer

val places = listOf(
    UserMapMarker(),
    UserMapMarker(),
    UserMapMarker(),
    UserMapMarker(),
    UserMapMarker()
)

@ExperimentalAnimationApi
@Composable
fun UserPlacesLoading(addNewPlaceClicked: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { ItemAddPlace(addNewPlaceClicked) }
        items(items = places) { userPlace ->
            ItemPlaceLoading(
                place = userPlace
            )
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun ItemPlaceLoading(place: UserMapMarker) {

    MyCard {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(75.dp).fillMaxWidth().padding(5.dp)
        ) {
            Row(modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(50.dp).padding(5.dp)) {
                    Icon(
                        painterResource(R.drawable.ic_baseline_location_on_24),
                        stringResource(R.string.place),
                        modifier = Modifier.align(Alignment.Center).padding(5.dp).fillMaxSize().shimmer(),
                        tint = Color.LightGray
                    )
                }
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
                ) {
                    Row(modifier = Modifier.height(18.dp).shimmer()) {Text(place.title, color = Color.LightGray, modifier = Modifier.background(Color.LightGray))}
                    Row(modifier = Modifier.height(18.dp).shimmer()) {Text("Great Place For fishing", color = Color.LightGray, modifier = Modifier.background(Color.LightGray))}
                }
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxHeight()
            ) {
                Icon(
                    painterResource(R.drawable.ic_fish),
                    stringResource(R.string.fish_catch),
                    modifier = Modifier.padding(2.dp).height(18.dp).shimmer(),
                    tint = Color.LightGray
                )
                Row(modifier = Modifier.height(18.dp).shimmer()) { Text("10", color = Color.LightGray, modifier = Modifier.background(Color.LightGray)) }
            }
        }
    }
}
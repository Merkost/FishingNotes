package com.joesemper.fishing.compose.ui.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.UserPreferences
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.notes.ItemUserCatch
import com.joesemper.fishing.compose.ui.navigate
import com.joesemper.fishing.compose.ui.theme.secondaryColor
import com.joesemper.fishing.compose.ui.theme.secondaryTextColor
import com.joesemper.fishing.domain.UserPlaceViewModel
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.compose.ui.theme.secondaryFigmaColor
import com.joesemper.fishing.compose.ui.theme.secondaryFigmaTextColor
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun UserPlaceScreen(navController: NavController, place: UserMapMarker?) {
    val viewModel = getViewModel<UserPlaceViewModel>()
    place?.let { viewModel.marker.value = it }

    val userPreferences: UserPreferences = get()
    val timeFormat by userPreferences.use12hTimeFormat.collectAsState(false)

    Scaffold(
        topBar = { UserPlaceAppBar(navController, viewModel) },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            AddNewCatchFab(onClick = { newCatchClicked(navController, viewModel) })
        }
    ) {
        viewModel.marker.value?.let { userPlace ->
            val userCatches by viewModel.getCatchesByMarkerId(userPlace.id)
                .collectAsState(listOf())

            ConstraintLayout(modifier = Modifier.padding(8.dp)) {
                val (title, icon, description, fishIcon, amount, catches) = createRefs()

                Icon(
                    modifier = Modifier
                        .padding(5.dp)
                        .size(32.dp)
                        .constrainAs(icon) {
                            top.linkTo(parent.top)
                            absoluteLeft.linkTo(parent.absoluteLeft)
                        },
                    painter = painterResource(R.drawable.ic_baseline_location_on_24),
                    contentDescription = stringResource(R.string.place),
                    tint = Color(userPlace.markerColor)
                )

                PrimaryTextBold(
                    modifier = Modifier.constrainAs(title) {
                        linkTo(icon.absoluteRight, amount.absoluteLeft, bias = 0f)
                        top.linkTo(parent.top)
                    },
                    text = userPlace.title
                )

                Icon(
                    modifier = Modifier.constrainAs(fishIcon) {
                        absoluteRight.linkTo(parent.absoluteRight, 4.dp)
                        top.linkTo(title.top)
                        bottom.linkTo(title.bottom)
                    },
                    painter = painterResource(id = R.drawable.ic_fish),
                    tint = secondaryFigmaTextColor,
                    contentDescription = stringResource(id = R.string.fish_catch)
                )

                PrimaryTextBold(
                    modifier = Modifier.constrainAs(amount) {
                        absoluteRight.linkTo(fishIcon.absoluteLeft, 2.dp)
                        top.linkTo(title.top)
                        bottom.linkTo(title.bottom)
                    },
                    text = userPlace.catchesCount.toString()
                )

                SecondaryText(
                    modifier = Modifier.constrainAs(description) {
                        top.linkTo(title.bottom)
                        absoluteLeft.linkTo(title.absoluteLeft)
                    },
                    text = if (userPlace.description.isNotBlank()) {
                        userPlace.description
                    } else {
                        stringResource(id = R.string.no_description)
                    }
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .constrainAs(catches) {
                            top.linkTo(description.bottom, 32.dp)
                            absoluteLeft.linkTo(parent.absoluteLeft)
                            absoluteRight.linkTo(parent.absoluteRight)
                        }) {
                    items(items = userCatches.sortedByDescending { it.date }) { item: UserCatch ->
                        ItemUserCatch(
                            userCatch = item,
                            timeFormat,
                            userCatchClicked = {
                                onCatchItemClick(
                                    catch = item,
                                    navController = navController
                                )
                            }
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun AddNewCatchFab(onClick: () -> Unit) {
    FloatingActionButton(
        backgroundColor = secondaryFigmaColor,
        onClick = { onClick() }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_add_catch),
            tint = Color.White,
            contentDescription = stringResource(
                id = R.string.add_new_catch
            )
        )
    }
}

//@Composable
//fun Buttons(navController: NavController) {
//    val viewModel = getViewModel<UserPlaceViewModel>()
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(60.dp)
//            .background(primaryFigmaColor),
//        horizontalArrangement = Arrangement.SpaceEvenly,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Button(
//            modifier = Modifier
//                .weight(1f)
//                .fillMaxSize(),
//            onClick = { routeClicked() },
//            border = BorderStroke(0.dp, color = Color.Transparent),
//            elevation = ButtonDefaults.elevation(0.dp)
//        ) {
//            Column() {
//                Icon(
//                    modifier = Modifier
//                        .align(Alignment.CenterHorizontally)
//                        .size(30.dp)
//                        .rotate(45f),
//                    painter = painterResource(R.drawable.ic_baseline_navigation_24),
//                    contentDescription = stringResource(R.string.navigate)
//                )
//                Text(fontSize = 10.sp, text = stringResource(R.string.navigate))
//            }
//        }
//        Button(
//            modifier = Modifier
//                .weight(1f)
//                .fillMaxSize(),
//            onClick = { shareClicked() },
//            border = BorderStroke(0.dp, color = Color.Transparent),
//            elevation = ButtonDefaults.elevation(0.dp)
//        ) {
//            Column() {
//                Icon(
//                    modifier = Modifier
//                        .align(Alignment.CenterHorizontally)
//                        .size(30.dp),
//                    painter = painterResource(R.drawable.ic_baseline_share_24),
//                    contentDescription = stringResource(R.string.share)
//                )
//                Text(fontSize = 10.sp, text = stringResource(R.string.share))
//            }
//        }
//        Button(
//            modifier = Modifier
//                .weight(1f)
//                .fillMaxSize(),
//            onClick = { newCatchClicked(navController, viewModel) },
//            border = BorderStroke(0.dp, color = Color.Transparent),
//            elevation = ButtonDefaults.elevation(0.dp)
//        ) {
//            Column() {
//                Icon(
//                    modifier = Modifier
//                        .align(Alignment.CenterHorizontally)
//                        .size(30.dp),
//                    painter = painterResource(R.drawable.ic_fish),
//                    contentDescription = stringResource(R.string.new_catch)
//                )
//                Text(fontSize = 10.sp, text = stringResource(R.string.new_catch))
//            }
//        }
//    }
//}

//@ExperimentalAnimationApi
//@Composable
//private fun EditPlaceInfo(user: User?, place: UserMapMarker) {
//    val viewModel = getViewModel<UserPlaceViewModel>()
//    MyCardNoPadding {
//        Column(
//            verticalArrangement = Arrangement.Top,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(10.dp)
//                .padding(horizontal = 5.dp)
//        ) {
//            Row(
//                modifier = Modifier
//                    .padding(horizontal = 10.dp)
//                    .height(50.dp)
//                    .fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(
//                    Icons.Default.Place,
//                    stringResource(R.string.place),
//                    tint = secondaryFigmaColor
//                )
//                Spacer(modifier = Modifier.width(150.dp))
//                UserProfile(user)
//            }
//            viewModel.titleTemp = rememberSaveable {
//                mutableStateOf(place.title)
//            }
//            viewModel.descriptionTemp = rememberSaveable {
//                mutableStateOf(place.description ?: "")
//            }
//            OutlinedTextField(
//                value = viewModel.titleTemp.value,
//                onValueChange = { viewModel.titleTemp.value = it },
//                label = { Text(stringResource(R.string.place)) },
//                singleLine = true, modifier = Modifier.fillMaxWidth()
//            )
//            OutlinedTextField(
//                value = viewModel.descriptionTemp.value ?: "",
//                onValueChange = { viewModel.descriptionTemp.value = it },
//                label = { Text(stringResource(R.string.description)) },
//                modifier = Modifier.fillMaxWidth()
//            )
//            Spacer(modifier = Modifier.size(8.dp))
//        }
//    }
//}

//@ExperimentalAnimationApi
//@Composable
//fun Catches(
//    catches: List<UserCatch>?,
//    catchClicked: (UserCatch) -> Unit,
//) {
//    catches?.let { userCatches ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            items(items = userCatches) {
//                ItemCatch(
//                    catch = it,
//                    catchClicked = catchClicked
//                )
//            }
//        }
//    } ?: CircularProgressIndicator()
//}


@Composable
fun UserPlaceAppBar(navController: NavController, viewModel: UserPlaceViewModel) {
    DefaultAppBar(
        title = stringResource(id = R.string.place),
        onNavClick = { navController.popBackStack() }
    ) {
        /*IconButton(onClick = { }) {
            Icon(imageVector = Icons.Filled.Edit, contentDescription = "", tint = Color.White)
        }*/
        IconButton(
            onClick = {
                viewModel.deletePlace()
                navController.popBackStack()
            }) {
            Icon(imageVector = Icons.Filled.Delete, contentDescription = "", tint = Color.White)
        }
        /*IconButton(onClick = { }) {
            Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "", tint = Color.White)
        }*/
    }
}

//@Composable
//fun DeleteDialog(dialogOnDelete: MutableState<Boolean>, navController: NavController) {
//    val viewModel = getViewModel<UserPlaceViewModel>()
//    AlertDialog(
//        title = { Text(stringResource(R.string.map_deletion)) },
//        text = { Text(stringResource(R.string.map_delete_confirmation)) },
//        onDismissRequest = { dialogOnDelete.value = false },
//        confirmButton = {
//            OutlinedButton(
//                onClick = {
//                    viewModel.deletePlace(viewModel.marker.value);
//                    navController.popBackStack()
//                    dialogOnDelete.value = false
//                },
//                content = { Text(stringResource(R.string.Yes)) })
//        }, dismissButton = {
//            OutlinedButton(
//                onClick = { dialogOnDelete.value = false },
//                content = { Text(stringResource(R.string.No)) })
//        }
//    )
//}

private fun newCatchClicked(navController: NavController, viewModel: UserPlaceViewModel) {
    val marker: UserMapMarker? = viewModel.marker.value
    marker?.let {
        navController.navigate(MainDestinations.NEW_CATCH_ROUTE, Arguments.PLACE to it)
    }
}


private fun routeClicked() {
    /*val viewModel = getViewModel<UserPlaceViewModel>()
    val uri = String.format(
        Locale.ENGLISH,
        "http://maps.google.com/maps?daddr=%f,%f (%s)",
        viewModel.marker.value.latitude,
        viewModel.marker.value.longitude,
        viewModel.title
    )
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    intent.setPackage("com.google.android.apps.maps")
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        try {
            val unrestrictedIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(unrestrictedIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Please install a maps application", Toast.LENGTH_LONG)
                .show()
        }
    }*/
}

private fun shareClicked() {
    /*val text =
        "${viewModel.title}\nhttps://www.google.com/maps/search/?api=1&query=${viewModel.marker.value.latitude}" +
                ",${viewModel.marker.value.longitude}"
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)*/
}

private fun onCatchItemClick(catch: UserCatch, navController: NavController) {
    navController.navigate(MainDestinations.CATCH_ROUTE, Arguments.CATCH to catch)
}
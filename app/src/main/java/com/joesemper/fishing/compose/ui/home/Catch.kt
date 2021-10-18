package com.joesemper.fishing.compose.ui.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.joesemper.fishing.R
import com.joesemper.fishing.domain.UserCatchViewModel
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.theme.secondaryFigmaColor
import com.joesemper.fishing.ui.theme.secondaryFigmaTextColor
import org.koin.androidx.compose.getViewModel

object Photo {
    private const val TAG = "CATCH"
    const val ITEM_PHOTO = "ITEM_PHOTO"
}

@ExperimentalAnimationApi
@Composable
fun UserCatchScreen(navController: NavController, catch: UserCatch?) {
    val viewModel = getViewModel<UserCatchViewModel>()

    catch?.let {
        Scaffold(topBar = {
            CatchTopBar(navController, catch)
        }) {
            CatchContent(catch = catch, viewModel = viewModel)
        }
    }
}

@Composable
fun CatchTopBar(navController: NavController, catch: UserCatch) {
    TopAppBar(contentColor = Color.White) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "", tint = Color.White)
        }
        Text(
            modifier = Modifier.padding(horizontal = 4.dp),
            color = Color.White,
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            text = stringResource(id = R.string.user_catch)
        )
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { }) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "", tint = Color.White)
            }
            IconButton(modifier = Modifier.padding(horizontal = 4.dp),
                onClick = { }) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = "", tint = Color.White)
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun CatchContent(catch: UserCatch, viewModel: UserCatchViewModel) {
    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxSize()
    ) {
        var mapMarker by remember {
            mutableStateOf(UserMapMarker())
        }

        viewModel.getMapMarker(catch.userMarkerId)
            .collectAsState(initial = UserMapMarker()).value?.let {
                mapMarker = it
            }

        Card(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            elevation = 4.dp
        ) {
            ConstraintLayout(modifier = Modifier.padding(8.dp)) {
                val (header, description, time, weight, amount, subtitle, rod, rodMeaning, lure, lureMeaning, bait, baitMeaning) = createRefs()

                HeaderText(
                    modifier = Modifier.constrainAs(header) {
                        top.linkTo(parent.top)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                    },
                    text = catch.fishType
                )

                HeaderText(
                    modifier = Modifier.constrainAs(weight) {
                        top.linkTo(header.top)
                        absoluteRight.linkTo(parent.absoluteRight)
                    },
                    text = catch.fishWeight.toString() + stringResource(id = R.string.kg)
                )

                SecondaryText(
                    modifier = Modifier.constrainAs(amount) {
                        top.linkTo(weight.bottom, 4.dp)
                        absoluteRight.linkTo(parent.absoluteRight)
                    },
                    text = catch.fishAmount.toString() + stringResource(id = R.string.pc)
                )

                SecondaryText(
                    modifier = Modifier.constrainAs(description) {
                        top.linkTo(header.bottom, 4.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                    },
                    text = catch.description
                )

                SubtitleText(
                    modifier = Modifier.constrainAs(subtitle) {
                        top.linkTo(description.bottom, 8.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)

                    },
                    text = stringResource(id = R.string.way_of_fishing)
                )

                PrimaryText(
                    modifier = Modifier.constrainAs(rodMeaning) {
                        top.linkTo(subtitle.bottom, 8.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                    },
                    text = if (catch.fishingRodType.isBlank()) {
                        stringResource(id = R.string.no_rod)
                    } else {
                        catch.fishingRodType
                    }
                )

                SecondaryText(
                    modifier = Modifier.constrainAs(rod) {
                        top.linkTo(rodMeaning.bottom)
                        absoluteLeft.linkTo(parent.absoluteLeft)

                    },
                    text = stringResource(id = R.string.fish_rod)
                )

                PrimaryText(
                    modifier = Modifier.constrainAs(baitMeaning) {
                        top.linkTo(rod.bottom, 8.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                    },
                    text = if (catch.fishingBait.isBlank()) {
                        stringResource(id = R.string.no_bait)
                    } else {
                        catch.fishingBait
                    }
                )

                SecondaryText(
                    modifier = Modifier.constrainAs(bait) {
                        top.linkTo(baitMeaning.bottom)
                        absoluteLeft.linkTo(parent.absoluteLeft)

                    },
                    text = stringResource(id = R.string.lure)
                )

                PrimaryText(
                    modifier = Modifier.constrainAs(lureMeaning) {
                        top.linkTo(bait.bottom, 8.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                    },
                    text = if (catch.fishingLure.isBlank()) {
                        stringResource(id = R.string.no_lure)
                    } else {
                        catch.fishingLure
                    }
                )

                SecondaryText(
                    modifier = Modifier.constrainAs(lure) {
                        top.linkTo(lureMeaning.bottom)
                        absoluteLeft.linkTo(parent.absoluteLeft)

                    },
                    text = stringResource(id = R.string.lure)
                )
            }
        }

        Card(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            elevation = 4.dp
        ) {
            ConstraintLayout(modifier = Modifier.padding(8.dp)) {
                val (subtitle, placeIcon, place, mapButton, description) = createRefs()

                SubtitleText(modifier = Modifier.constrainAs(subtitle) {
                    top.linkTo(parent.top)
                    absoluteLeft.linkTo(parent.absoluteLeft)

                }, text = stringResource(id = R.string.place))

                Icon(
                    modifier = Modifier.constrainAs(placeIcon) {
                        top.linkTo(subtitle.bottom, 16.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                    },
                    painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                    tint = secondaryFigmaColor,
                    contentDescription = ""
                )

                PrimaryText(
                    modifier = Modifier.constrainAs(place) {
                        top.linkTo(placeIcon.top)
                        bottom.linkTo(placeIcon.bottom)
                        absoluteLeft.linkTo(placeIcon.absoluteRight, 4.dp)
                    },
                    text = mapMarker.title
                )

                SecondaryText(modifier = Modifier.constrainAs(description) {
                    top.linkTo(place.bottom, 4.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                }, text = mapMarker.description ?: stringResource(id = R.string.no_description))

                IconButton(modifier = Modifier.constrainAs(mapButton) {
                    top.linkTo(place.top)
                    bottom.linkTo(place.bottom)
                    absoluteRight.linkTo(parent.absoluteRight)
                },
                    onClick = { /*TODO*/ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_map_24),
                        tint = secondaryFigmaTextColor,
                        contentDescription = ""
                    )
                }


            }
        }

        Card(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                SubtitleText(
                    modifier = Modifier,
                    text = stringResource(id = R.string.photos)
                )
                Spacer(modifier = Modifier.padding(8.dp))
                if (catch.downloadPhotoLinks.isNotEmpty()) {
                    LazyRow() {
                        items(items = catch.downloadPhotoLinks) {
                            ItemPhoto(photo = it.toUri(), clickedPhoto = {}, deletedPhoto = {})
                        }

                    }
                } else {
                    SecondaryText(text = stringResource(id = R.string.no_photo_selected))
                }
                Spacer(modifier = Modifier.padding(8.dp))

            }
        }

        Card(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            elevation = 4.dp
        ) {
            ConstraintLayout(modifier = Modifier.padding(8.dp)) {
                val (subtitle, weather) = createRefs()

                SubtitleText(
                    modifier = Modifier.constrainAs(subtitle) {
                        top.linkTo(parent.top)
                        absoluteLeft.linkTo(parent.absoluteLeft)

                    },
                    text = stringResource(id = R.string.weather)
                )

                PrimaryText(
                    modifier = Modifier.constrainAs(weather) {
                        top.linkTo(parent.top)
                        absoluteLeft.linkTo(parent.absoluteLeft)

                    },
                    text = ""
                )

            }

        }

    }
}

//    catch?.let { viewModel.catch.value = it }
//    Scaffold(
//        topBar = { CatchAppBar(navController) }
//    ) {
//        val scrollState = rememberScrollState()
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(14.dp),
//            modifier = Modifier
//                .fillMaxSize()
//                .background(primaryFigmaBackgroundTint)
//                .verticalScroll(state = scrollState, enabled = true),
//        ) {
//            val user by viewModel.getCurrentUser().collectAsState(null)
//            CatchInfo(viewModel.catch.value, user)
//            Photos(viewModel.catch.value)
//            val mapMarker by viewModel.getMapMarker(viewModel.catch.value.userMarkerId)
//                .collectAsState(null)
//            mapMarker?.let { it1 -> PlaceInfo(user, it1) { onPlaceClicked(it, navController)} }
//            MyTextField(
//                stringResource(R.string.weight),
//                viewModel.catch.value.fishWeight.toString() + " " + stringResource(R.string.kg)
//            )
//            MyTextField(
//                stringResource(R.string.amount),
//                viewModel.catch.value.fishAmount.toString() + " " + stringResource(R.string.pc)
//            )
//            /*MyTextField(stringResource(R.string.date), catch.date)
//            MyTextField(stringResource(R.string.time), catch.time)*/
//            MyTextField(
//                stringResource(R.string.fish_rod),
//                if (!viewModel.catch.value.fishingRodType.isEmpty())
//                    viewModel.catch.value.fishingRodType else stringResource(R.string.not_specified)
//            )
//            MyTextField(
//                stringResource(R.string.bait), if (!viewModel.catch.value.fishingBait.isEmpty())
//                    viewModel.catch.value.fishingBait else stringResource(R.string.not_specified)
//            )
//            MyTextField(
//                stringResource(R.string.lure), if (!viewModel.catch.value.fishingLure.isEmpty())
//                    viewModel.catch.value.fishingLure else stringResource(R.string.not_specified)
//            )
//            Spacer(Modifier.size(5.dp))
//        }
//
//    }
//}
//
//@Composable
//fun Photos(
//    catch: UserCatch
//    //clickedPhoto: SnapshotStateList<Painter>
//) {
//    LazyRow(modifier = Modifier.fillMaxSize()) {
//        item { Spacer(modifier = Modifier.size(4.dp)) }
//        if (catch.downloadPhotoLinks.isNullOrEmpty()) {
//            item { Text("No photos here", modifier = Modifier.padding(horizontal = 11.dp)) }
//        } else {
//            items(items = catch.downloadPhotoLinks) {
//                ItemPhoto(
//                    photo = it,
//                    //clickedPhoto = clickedPhoto
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun ItemNoPhoto() {
//
//
//    /*Box(
//        modifier = Modifier
//            .size(100.dp)
//            .padding(4.dp)
//    ) {
//        Card(
//            modifier = Modifier
//                .fillMaxSize()
//                .clip(RoundedCornerShape(5.dp))
//                .clickable { showToast(requireContext(),
//                    "На этот улов не были добавлены фото, к сожалению.") },
//            elevation = 5.dp, backgroundColor = Color.LightGray
//        ) {
//            Icon(
//                painterResource(R.drawable.ic_no_photo_vector), //Or we can use Icons.Default.Add
//                contentDescription = "NO_PHOTOS",
//                tint = Color.White,
//                modifier = Modifier
//                    .fillMaxSize()
//                    .align(Alignment.Center)
//            )
//        }
//    }*/
//}
//
//@Composable
//fun ItemPhoto(
//    photo: String,
//    //clickedPhoto: (Painter) -> Unit
//) {
//    Box(
//        modifier = Modifier
//            .size(100.dp)
//            .padding(4.dp)
//    ) {
//        Image(painter = rememberImagePainter(data = photo),
//            contentDescription = Photo.ITEM_PHOTO,
//            contentScale = ContentScale.Crop,
//            modifier = Modifier
//                .fillMaxWidth()
//                .clip(RoundedCornerShape(5.dp))
//                .clickable { /*clickedPhoto(photo)*/ })
//    }
//}
//
//@Composable
//private fun MyTextField(text: String, info: String) {
//    MyCardNoPadding {
//        Row(
//            horizontalArrangement = Arrangement.SpaceBetween,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 10.dp, vertical = 4.dp)
//                .height(40.dp)
//        ) {
//            Text(
//                text,
//                modifier = Modifier
//                    .padding(start = 5.dp)
//                    .align(Alignment.CenterVertically)
//            )
//            Text(
//                info,
//                modifier = Modifier
//                    .padding(end = 5.dp)
//                    .align(Alignment.CenterVertically)
//            )
//        }
//    }
//}
//
//@Composable
//fun CatchAppBar(navController: NavController) {
//    val dialogOnDelete = rememberSaveable { mutableStateOf(false) }
//    TopAppBar(
//        title = { Text(text = stringResource(R.string.user_catch)) },
//        navigationIcon = {
//            IconButton(onClick = {
//                navController.popBackStack(
//                    /*MainDestinations.CATCH_ROUTE,
//                    inclusive = true*/
//                )
//            }, content = {
//                Icon(
//                    imageVector = Icons.Filled.ArrowBack,
//                    contentDescription = stringResource(R.string.back)
//                )
//            })
//        }, actions = {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(end = 3.dp),
//                horizontalArrangement = Arrangement.End,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                IconButton(
//                    onClick = {
//                        /*showToast(
//                            requireContext(),
//                            "Not Yet Implemented"
//                        )*/
//                    },
//                    content = { Icon(Icons.Default.Edit, stringResource(R.string.edit)) }
//                )
//                IconButton(
//                    onClick = {
//                        dialogOnDelete.value = true
//                    },
//                    content = { Icon(Icons.Default.Delete, stringResource(R.string.edit)) }
//                )
//            }
//        })
//    if (dialogOnDelete.value) DeleteDialogCatch(dialogOnDelete, navController)
//}
//
//@Composable
//fun DeleteDialogCatch(dialogOnDelete: MutableState<Boolean>, navController: NavController) {
//    val viewModel = getViewModel<UserCatchViewModel>()
//    AlertDialog(
//        title = { Text(stringResource(R.string.catch_deletion)) },
//        text = { Text(stringResource(R.string.catch_delete_confirmantion)) },
//        onDismissRequest = { dialogOnDelete.value = false },
//        confirmButton = {
//            OutlinedButton(
//                onClick = {
//                    viewModel.deleteCatch();
//                    navController.popBackStack()
//                    //navController.popBackStack(MainDestinations.CATCH_ROUTE, inclusive = true)
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
//
//fun onPlaceClicked(place: UserMapMarker, navController: NavController) {
//    navController.currentBackStackEntry?.arguments?.putParcelable(Arguments.PLACE, place)
//    navController.navigate(MainDestinations.PLACE_ROUTE)
//}
package com.joesemper.fishing.compose.ui.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.domain.UserCatchViewModel
import com.joesemper.fishing.domain.UserPlaceViewModel
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.theme.primaryFigmaBackgroundTint
import com.joesemper.fishing.utils.showToast
import org.koin.androidx.compose.getViewModel

object Photo {
    private const val TAG = "CATCH"
    const val ITEM_PHOTO = "ITEM_PHOTO"
}

@Composable
fun UserCatchScreen(navController: NavController, catch: UserCatch?) {
    val viewModel = getViewModel<UserCatchViewModel>()
    catch?.let { viewModel.catch.value = it }
    Scaffold(
        topBar = { CatchAppBar(navController) }
    ) {
        val scrollState = rememberScrollState()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(primaryFigmaBackgroundTint)
                .verticalScroll(state = scrollState, enabled = true),
        ) {
            val user by viewModel.getCurrentUser().collectAsState(null)
            CatchInfo(viewModel.catch.value, user)
            Photos(viewModel.catch.value)
            val mapMarker by viewModel.getMapMarker(viewModel.catch.value.userMarkerId)
                .collectAsState(null)
            mapMarker?.let { it1 -> PlaceInfo(user, it1) { onPlaceClicked(it, navController)} }
            MyTextField(
                stringResource(R.string.weight),
                viewModel.catch.value.fishWeight.toString() + " " + stringResource(R.string.kg)
            )
            MyTextField(
                stringResource(R.string.amount),
                viewModel.catch.value.fishAmount.toString() + " " + stringResource(R.string.pc)
            )
            /*MyTextField(stringResource(R.string.date), catch.date)
            MyTextField(stringResource(R.string.time), catch.time)*/
            MyTextField(
                stringResource(R.string.fish_rod),
                if (!viewModel.catch.value.fishingRodType.isEmpty())
                    viewModel.catch.value.fishingRodType else stringResource(R.string.not_specified)
            )
            MyTextField(
                stringResource(R.string.bait), if (!viewModel.catch.value.fishingBait.isEmpty())
                    viewModel.catch.value.fishingBait else stringResource(R.string.not_specified)
            )
            MyTextField(
                stringResource(R.string.lure), if (!viewModel.catch.value.fishingLure.isEmpty())
                    viewModel.catch.value.fishingLure else stringResource(R.string.not_specified)
            )
            Spacer(Modifier.size(5.dp))
        }

    }
}

@Composable
fun Photos(
    catch: UserCatch
    //clickedPhoto: SnapshotStateList<Painter>
) {
    LazyRow(modifier = Modifier.fillMaxSize()) {
        item { Spacer(modifier = Modifier.size(4.dp)) }
        if (catch.downloadPhotoLinks.isNullOrEmpty()) {
            item { Text("No photos here", modifier = Modifier.padding(horizontal = 11.dp)) }
        } else {
            items(items = catch.downloadPhotoLinks) {
                ItemPhoto(
                    photo = it,
                    //clickedPhoto = clickedPhoto
                )
            }
        }
    }
}

@Composable
fun ItemNoPhoto() {


    /*Box(
        modifier = Modifier
            .size(100.dp)
            .padding(4.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(5.dp))
                .clickable { showToast(requireContext(),
                    "На этот улов не были добавлены фото, к сожалению.") },
            elevation = 5.dp, backgroundColor = Color.LightGray
        ) {
            Icon(
                painterResource(R.drawable.ic_no_photo_vector), //Or we can use Icons.Default.Add
                contentDescription = "NO_PHOTOS",
                tint = Color.White,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )
        }
    }*/
}

@Composable
fun ItemPhoto(
    photo: String,
    //clickedPhoto: (Painter) -> Unit
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .padding(4.dp)
    ) {
        Image(painter = rememberImagePainter(data = photo),
            contentDescription = Photo.ITEM_PHOTO,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(5.dp))
                .clickable { /*clickedPhoto(photo)*/ })
    }
}

@Composable
private fun MyTextField(text: String, info: String) {
    MyCardNoPadding {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .height(40.dp)
        ) {
            Text(
                text,
                modifier = Modifier
                    .padding(start = 5.dp)
                    .align(Alignment.CenterVertically)
            )
            Text(
                info,
                modifier = Modifier
                    .padding(end = 5.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
fun CatchAppBar(navController: NavController) {
    val dialogOnDelete = rememberSaveable { mutableStateOf(false) }
    TopAppBar(
        title = { Text(text = stringResource(R.string.user_catch)) },
        navigationIcon = {
            IconButton(onClick = {
                navController.popBackStack(
                    /*MainDestinations.CATCH_ROUTE,
                    inclusive = true*/
                )
            }, content = {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            })
        }, actions = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 3.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        /*showToast(
                            requireContext(),
                            "Not Yet Implemented"
                        )*/
                    },
                    content = { Icon(Icons.Default.Edit, stringResource(R.string.edit)) }
                )
                IconButton(
                    onClick = {
                        dialogOnDelete.value = true
                    },
                    content = { Icon(Icons.Default.Delete, stringResource(R.string.edit)) }
                )
            }
        })
    if (dialogOnDelete.value) DeleteDialogCatch(dialogOnDelete, navController)
}

@Composable
fun DeleteDialogCatch(dialogOnDelete: MutableState<Boolean>, navController: NavController) {
    val viewModel = getViewModel<UserCatchViewModel>()
    AlertDialog(
        title = { Text(stringResource(R.string.catch_deletion)) },
        text = { Text(stringResource(R.string.catch_delete_confirmantion)) },
        onDismissRequest = { dialogOnDelete.value = false },
        confirmButton = {
            OutlinedButton(
                onClick = {
                    viewModel.deleteCatch();
                    navController.popBackStack()
                    //navController.popBackStack(MainDestinations.CATCH_ROUTE, inclusive = true)
                    dialogOnDelete.value = false
                },
                content = { Text(stringResource(R.string.Yes)) })
        }, dismissButton = {
            OutlinedButton(
                onClick = { dialogOnDelete.value = false },
                content = { Text(stringResource(R.string.No)) })
        }
    )
}

fun onPlaceClicked(place: UserMapMarker, navController: NavController) {
    navController.currentBackStackEntry?.arguments?.putParcelable(Arguments.PLACE, place)
    navController.navigate(MainDestinations.PLACE_ROUTE)
}
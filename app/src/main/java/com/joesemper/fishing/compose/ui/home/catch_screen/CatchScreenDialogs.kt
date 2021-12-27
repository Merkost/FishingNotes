package com.joesemper.fishing.compose.ui.home.catch_screen

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.Constants
import com.joesemper.fishing.compose.ui.home.FishAmountAndWeightView
import com.joesemper.fishing.compose.ui.home.notes.ItemPhoto
import com.joesemper.fishing.compose.ui.home.views.DefaultDialog
import com.joesemper.fishing.compose.ui.home.views.MaxCounterView
import com.joesemper.fishing.compose.ui.home.views.PrimaryTextSmall
import com.joesemper.fishing.compose.ui.home.views.SimpleOutlinedTextField
import com.joesemper.fishing.domain.UserCatchViewModel
import com.joesemper.fishing.model.entity.content.UserCatch

@ExperimentalComposeUiApi
@Composable
fun FishTypeAmountAndWeightDialog(
    catch: UserCatch,
    dialogState: MutableState<Boolean>,
    viewModel: UserCatchViewModel
) {

    val fishType = remember { mutableStateOf(catch.fishType) }
    val fishAmount = remember { mutableStateOf(catch.fishAmount.toString()) }
    val fishWeight = remember { mutableStateOf(catch.fishWeight.toString()) }

    DefaultDialog(
        primaryText = stringResource(id = R.string.user_catch),
        positiveButtonText = stringResource(id = R.string.save),
        negativeButtonText = stringResource(id = R.string.cancel),
        onNegativeClick = { dialogState.value = false },
        onPositiveClick = {
            viewModel.updateCatch(
                data = mapOf(
                    "fishType" to fishType.value,
                    "fishAmount" to fishAmount.value.toInt(),
                    "fishWeight" to fishWeight.value.toDouble()
                )
            )
            dialogState.value = false
        },
        onDismiss = { dialogState.value = false },
    ) {

        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SimpleOutlinedTextField(
                textState = fishType,
                label = stringResource(id = R.string.fish_species)
            )
            FishAmountAndWeightView(
                amountState = fishAmount,
                weightState = fishWeight
            )

        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun EditWayOfFishingDialog(
    catch: UserCatch,
    dialogState: MutableState<Boolean>,
    viewModel: UserCatchViewModel
) {
    val rodState = remember { mutableStateOf(catch.fishingRodType) }
    val baitState = remember { mutableStateOf(catch.fishingBait) }
    val lureState = remember { mutableStateOf(catch.fishingLure) }

    DefaultDialog(
        primaryText = stringResource(id = R.string.way_of_fishing),
        positiveButtonText = stringResource(id = R.string.save),
        negativeButtonText = stringResource(id = R.string.cancel),
        onNegativeClick = { dialogState.value = false },
        onPositiveClick = {
            viewModel.updateCatch(
                data = mapOf(
                    "fishingRodType" to rodState.value,
                    "fishingBait" to baitState.value,
                    "fishingLure" to lureState.value
                )
            )
            dialogState.value = false
        },
        onDismiss = { dialogState.value = false },
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SimpleOutlinedTextField(
                textState = rodState,
                label = stringResource(id = R.string.fish_rod),
                singleLine = false
            )
            SimpleOutlinedTextField(
                textState = baitState,
                label = stringResource(id = R.string.bait),
                singleLine = false
            )
            SimpleOutlinedTextField(
                textState = lureState,
                label = stringResource(id = R.string.lure),
                singleLine = false
            )

        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun EditNoteDialog(
    note: String,
    dialogState: MutableState<Boolean>,
    onSaveNoteChange: (String) -> Unit
) {
    val noteState = remember { mutableStateOf(note) }

    DefaultDialog(
        primaryText = stringResource(id = R.string.note),
        positiveButtonText = stringResource(id = R.string.save),
        negativeButtonText = stringResource(id = R.string.cancel),
        onNegativeClick = { dialogState.value = false },
        onPositiveClick = {
            onSaveNoteChange(noteState.value)
            dialogState.value = false
        },
        onDismiss = { dialogState.value = false },
    ) {
        Column(
            modifier = Modifier

                .fillMaxWidth()
                .wrapContentHeight()
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SimpleOutlinedTextField(
                textState = noteState,
                label = stringResource(id = R.string.note),
                singleLine = false
            )

        }
    }
}

@ExperimentalAnimationApi
@ExperimentalPermissionsApi
@ExperimentalComposeUiApi
@Composable
fun AddPhotoDialog(
    photos: List<Uri>,
    dialogState: MutableState<Boolean>,
    onSavePhotosClick: (List<Uri>) -> Unit
) {
    val context = LocalContext.current

    val tempDialogPhotosState = remember { mutableStateListOf<Uri>() }

    val permissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)

    val addPhotoState = rememberSaveable { mutableStateOf(false) }

    val choosePhotoLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { value ->
            if ((value.size + tempDialogPhotosState.size) > Constants.MAX_PHOTOS) {
                tempDialogPhotosState.addAll(value.takeLast(tempDialogPhotosState.size-value.size))
                Toast.makeText(context, "5 photos maximum allowed", Toast.LENGTH_SHORT).show()
            } else {
                tempDialogPhotosState.addAll(value)
            }
        }

    LaunchedEffect(key1 = photos) {
        if (photos.isEmpty()) {
            addPhotoState.value = true
        } else {
            tempDialogPhotosState.addAll(photos)
        }
    }

    DefaultDialog(
        primaryText = stringResource(id = R.string.photos),
        positiveButtonText = stringResource(id = R.string.save),
        negativeButtonText = stringResource(id = R.string.cancel),
        neutralButtonText = stringResource(id = R.string.add),
        onNegativeClick = {
            dialogState.value = false
        },
        onPositiveClick = {
            onSavePhotosClick(tempDialogPhotosState)
            dialogState.value = false
        },
        onDismiss = {
            dialogState.value = false
        },
        onNeutralClick = {
            if (tempDialogPhotosState.size >= Constants.MAX_PHOTOS) {
                Toast.makeText(context, "5 photos maximum allowed", Toast.LENGTH_SHORT).show()
            } else {
                addPhotoState.value = true
            }
        }
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val (counter, content, addButton) = createRefs()

            MaxCounterView(
                modifier = Modifier.constrainAs(counter) {
                    absoluteRight.linkTo(parent.absoluteRight)
                },
                count = tempDialogPhotosState.size,
                maxCount = Constants.MAX_PHOTOS,
                icon = painterResource(id = R.drawable.ic_baseline_photo_24)
            )

            LazyRow(
                modifier = Modifier.constrainAs(content) {
                    top.linkTo(counter.bottom, 8.dp)
                    bottom.linkTo(parent.bottom, 8.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                    width = Dimension.fillToConstraints
                },
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tempDialogPhotosState.isNotEmpty()) {
                    items(items = tempDialogPhotosState) {
                        ItemPhoto(
                            photo = it,
                            clickedPhoto = { },
                            deletedPhoto = { tempDialogPhotosState.remove(it) }
                        )
                    }
                } else {
                    item {
                        PrimaryTextSmall(
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            text = stringResource(id = R.string.no_photos_added)
                        )
                    }
                }
            }
        }
    }

    if (addPhotoState.value) {
        LaunchedEffect(addPhotoState) {
            permissionState.launchPermissionRequest()
        }
        addPhoto(permissionState, addPhotoState, choosePhotoLauncher)
    }
}

@ExperimentalPermissionsApi
fun addPhoto(
    permissionState: PermissionState,
    addPhotoState: MutableState<Boolean>,
    choosePhotoLauncher: ManagedActivityResultLauncher<Array<String>, List<Uri>>
) {
    when {
        permissionState.hasPermission -> {
            choosePhotoLauncher.launch(arrayOf("image/*"))
            addPhotoState.value = false
        }
    }
}

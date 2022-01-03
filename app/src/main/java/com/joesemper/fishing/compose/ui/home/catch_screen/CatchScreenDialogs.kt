package com.joesemper.fishing.compose.ui.home.catch_screen

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.net.toUri
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.advertising.showInterstitialAd
import com.joesemper.fishing.compose.ui.home.new_catch.FishAmountAndWeightView
import com.joesemper.fishing.compose.ui.home.notes.ItemPhoto
import com.joesemper.fishing.compose.ui.home.views.*
import com.joesemper.fishing.domain.UserCatchViewModel
import com.joesemper.fishing.model.entity.common.Note
import com.joesemper.fishing.utils.Constants.MAX_PHOTOS


sealed class BottomSheetCatchScreen() {
    object EditFishTypeAndWeightScreen : BottomSheetCatchScreen()
    object EditNoteScreen : BottomSheetCatchScreen()
    object EditPhotosScreen : BottomSheetCatchScreen()
    object EditWayOfFishingScreen : BottomSheetCatchScreen()
}

@ExperimentalPermissionsApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun CatchModalBottomSheetContent(
    currentScreen: BottomSheetCatchScreen,
    viewModel: UserCatchViewModel,
    onCloseBottomSheet: () -> Unit,
) {
    val context = LocalContext.current

    when (currentScreen) {
        BottomSheetCatchScreen.EditFishTypeAndWeightScreen -> {
            FishTypeAmountAndWeightDialog(
                viewModel = viewModel,
                onCloseBottomSheet = onCloseBottomSheet
            )
        }

        BottomSheetCatchScreen.EditNoteScreen -> {
            EditNoteDialog(
                note = viewModel.catch.value?.note ?: Note(),
                onSaveNote = { note ->
                    viewModel.updateCatch(data = mapOf("note" to note))
                },
                onCloseDialog = onCloseBottomSheet
            )

        }

        BottomSheetCatchScreen.EditPhotosScreen -> {
            AddPhotoDialog(
                photos = viewModel.catch.value?.downloadPhotoLinks?.map { it.toUri() } ?: listOf(),
                onSavePhotosClick = { newPhotos ->
                    viewModel.updateCatchPhotos(newPhotos)
                    if (newPhotos.find { !it.toString().startsWith("http") } != null) {
                        showInterstitialAd(
                            context = context,
                            onAdLoaded = { }
                        )
                    }
                },
                onCloseBottomSheet = onCloseBottomSheet
            )
        }

        BottomSheetCatchScreen.EditWayOfFishingScreen -> {
            EditWayOfFishingDialog(
                viewModel = viewModel,
                onCloseBottomSheet = onCloseBottomSheet
            )
        }

    }
}

@ExperimentalComposeUiApi
@Composable
fun FishTypeAmountAndWeightDialog(
    viewModel: UserCatchViewModel,
    onCloseBottomSheet: () -> Unit
) {

    val fishType = remember { mutableStateOf("") }
    val fishAmount = remember { mutableStateOf("") }
    val fishWeight = remember { mutableStateOf("") }

    LaunchedEffect(key1 = viewModel.catch.value) {
        viewModel.catch.value?.let {
            fishType.value = it.fishType
            fishAmount.value = it.fishAmount.toString()
            fishWeight.value = it.fishWeight.toString()
        }
    }

    ConstraintLayout(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .fillMaxHeight(),
    ) {

        val (title, fish, amountAndWeight, saveButton, cancelButton) = createRefs()

        PrimaryText(
            modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top)
                absoluteLeft.linkTo(parent.absoluteLeft)
            },
            text = stringResource(id = R.string.user_catch)
        )

        SimpleOutlinedTextField(
            modifier = Modifier.constrainAs(fish) {
                top.linkTo(title.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
                width = Dimension.fillToConstraints
            },
            textState = fishType,
            label = stringResource(id = R.string.fish_species)
        )

        FishAmountAndWeightView(
            modifier = Modifier.constrainAs(amountAndWeight) {
                top.linkTo(fish.bottom, 8.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
                width = Dimension.fillToConstraints
            },
            amountState = fishAmount,
            weightState = fishWeight
        )

        DefaultButtonFilled(
            modifier = Modifier.constrainAs(saveButton) {
                top.linkTo(amountAndWeight.bottom, 16.dp)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            text = stringResource(id = R.string.save),
            onClick = {
                viewModel.updateCatch(
                    data = mapOf(
                        "fishType" to fishType.value,
                        "fishAmount" to fishAmount.value.toInt(),
                        "fishWeight" to fishWeight.value.toDouble()
                    )
                )
                onCloseBottomSheet()
            }
        )

        DefaultButton(
            modifier = Modifier.constrainAs(cancelButton) {
                top.linkTo(saveButton.top)
                bottom.linkTo(saveButton.bottom)
                absoluteRight.linkTo(saveButton.absoluteLeft, 8.dp)
            },
            text = stringResource(id = R.string.cancel),
            onClick = { onCloseBottomSheet() }
        )
    }

}

@ExperimentalComposeUiApi
@Composable
fun EditWayOfFishingDialog(
    viewModel: UserCatchViewModel,
    onCloseBottomSheet: () -> Unit
) {
    val rod = remember { mutableStateOf("") }
    val bait = remember { mutableStateOf("") }
    val lure = remember { mutableStateOf("") }

    LaunchedEffect(key1 = viewModel.catch.value) {
        viewModel.catch.value?.let {
            rod.value = it.fishingRodType
            bait.value = it.fishingBait
            lure.value = it.fishingLure
        }
    }

    ConstraintLayout(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        val (title, rodField, baitField, lureField, saveButton, cancelButton) = createRefs()

        PrimaryText(
            modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top)
                absoluteLeft.linkTo(parent.absoluteLeft)
            },
            text = stringResource(id = R.string.way_of_fishing)
        )

        SimpleOutlinedTextField(
            modifier = Modifier.constrainAs(rodField) {
                top.linkTo(title.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
                width = Dimension.fillToConstraints
            },
            textState = rod,
            label = stringResource(id = R.string.fish_rod),
            singleLine = false
        )

        SimpleOutlinedTextField(
            modifier = Modifier.constrainAs(baitField) {
                top.linkTo(rodField.bottom, 8.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
                width = Dimension.fillToConstraints
            },
            textState = bait,
            label = stringResource(id = R.string.bait),
            singleLine = false
        )

        SimpleOutlinedTextField(
            modifier = Modifier.constrainAs(lureField) {
                top.linkTo(baitField.bottom, 8.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
                width = Dimension.fillToConstraints
            },
            textState = lure,
            label = stringResource(id = R.string.lure),
            singleLine = false
        )

        DefaultButtonFilled(
            modifier = Modifier.constrainAs(saveButton) {
                top.linkTo(lureField.bottom, 16.dp)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            text = stringResource(id = R.string.save),
            onClick = {
                viewModel.updateCatch(
                    data = mapOf(
                        "fishingRodType" to rod.value,
                        "fishingBait" to bait.value,
                        "fishingLure" to lure.value
                    )
                )
                onCloseBottomSheet()
            }
        )

        DefaultButton(
            modifier = Modifier.constrainAs(cancelButton) {
                top.linkTo(saveButton.top)
                bottom.linkTo(saveButton.bottom)
                absoluteRight.linkTo(saveButton.absoluteLeft, 8.dp)
            },
            text = stringResource(id = R.string.cancel),
            onClick = { onCloseBottomSheet() }
        )

    }
}

@ExperimentalComposeUiApi
@Composable
fun EditNoteDialog(
    note: Note,
    onSaveNote: (Note) -> Unit,
    onCloseDialog: () -> Unit
) {
    val note = remember { mutableStateOf(note) }

    val noteId = remember { mutableStateOf(note.value.id) }
    val noteTitle = remember { mutableStateOf(note.value.title) }
    val noteDescriptionState = remember { mutableStateOf(note.value.description) }
    val noteDateCreated = remember { mutableStateOf(note.value.dateCreated) }

    val description = noteDescriptionState.value

    ConstraintLayout(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        val (title, editNote, saveButton, cancelButton) = createRefs()

        PrimaryText(
            modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top)
                absoluteLeft.linkTo(parent.absoluteLeft)
            },
            text = stringResource(id = R.string.edit_note)
        )

        SimpleOutlinedTextField(
            modifier = Modifier.constrainAs(editNote) {
                top.linkTo(title.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
                width = Dimension.fillToConstraints
            },
            textState = noteDescriptionState,
            label = stringResource(id = R.string.note),
            singleLine = false
        )

        DefaultButtonFilled(
            modifier = Modifier.constrainAs(saveButton) {
                top.linkTo(editNote.bottom, 16.dp)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            text = stringResource(id = R.string.save),
            onClick = {
                onSaveNote(
                    Note(
                        noteId.value,
                        noteTitle.value,
                        noteDescriptionState.value,
                        noteDateCreated.value
                    )
                )
                onCloseDialog()
            }
        )

        DefaultButton(
            modifier = Modifier.constrainAs(cancelButton) {
                top.linkTo(saveButton.top)
                bottom.linkTo(saveButton.bottom)
                absoluteRight.linkTo(saveButton.absoluteLeft, 8.dp)
            },
            text = stringResource(id = R.string.cancel),
            onClick = { onCloseDialog() }
        )

    }
}

@ExperimentalAnimationApi
@ExperimentalPermissionsApi
@ExperimentalComposeUiApi
@Composable
fun AddPhotoDialog(
    photos: List<Uri>,
    onSavePhotosClick: (List<Uri>) -> Unit,
    onCloseBottomSheet: () -> Unit
) {
    val context = LocalContext.current

    val tempDialogPhotosState = remember { mutableStateListOf<Uri>() }

    val permissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)

    val addPhotoState = rememberSaveable { mutableStateOf(false) }

    val choosePhotoLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { value ->
            if ((value.size + tempDialogPhotosState.size) > MAX_PHOTOS) {
                Toast.makeText(context, "5 photos maximum allowed", Toast.LENGTH_SHORT).show()
            }
            tempDialogPhotosState.addAll(value)
        }

    LaunchedEffect(key1 = photos) {
        if (photos.isEmpty()) {
            addPhotoState.value = true
        } else {
            tempDialogPhotosState.addAll(photos)
        }
    }


    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        val (title, counter, content, addButton, saveButton, cancelButton) = createRefs()

        PrimaryText(
            modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
            },
            text = stringResource(id = R.string.photos)
        )

        MaxCounterView(
            modifier = Modifier.constrainAs(counter) {
                top.linkTo(title.bottom, 8.dp)
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
            },
            count = tempDialogPhotosState.size,
            maxCount = MAX_PHOTOS,
            icon = painterResource(id = R.drawable.ic_baseline_photo_24)
        )

        LazyRow(
            modifier = Modifier
                .defaultMinSize(minHeight = 150.dp)
                .constrainAs(content) {
                    top.linkTo(counter.bottom, 8.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                    width = Dimension.fillToConstraints
                },
            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
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
                    NoContentView(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.no_photos_added),
                        icon = painterResource(id = R.drawable.ic_no_photos)
                    )
                }
            }
        }

        DefaultButtonFilled(
            modifier = Modifier.constrainAs(saveButton) {
                top.linkTo(content.bottom, 16.dp)
                bottom.linkTo(parent.bottom, 32.dp)
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
            },
            text = stringResource(id = R.string.save),
            onClick = {
                if (tempDialogPhotosState.size > MAX_PHOTOS) {
                    Toast.makeText(context, "5 photos maximum allowed", Toast.LENGTH_SHORT).show()
                } else {
                    onSavePhotosClick(tempDialogPhotosState)
                    onCloseBottomSheet()
                }
            }
        )

        DefaultButton(
            modifier = Modifier.constrainAs(cancelButton) {
                top.linkTo(saveButton.top)
                bottom.linkTo(saveButton.bottom)
                absoluteRight.linkTo(saveButton.absoluteLeft, 8.dp)
            },
            text = stringResource(id = R.string.cancel),
            onClick = onCloseBottomSheet
        )

        DefaultButtonOutlined(
            modifier = Modifier.constrainAs(addButton) {
                top.linkTo(saveButton.top)
                bottom.linkTo(saveButton.bottom)
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
            },
            icon = painterResource(id = R.drawable.ic_baseline_add_photo_alternate_24),
            text = stringResource(id = R.string.add),
            onClick = { addPhotoState.value = true }
        )

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


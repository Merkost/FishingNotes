package com.mobileprism.fishing.ui.home.catch

import android.Manifest
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.net.toUri
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.ui.home.new_catch.weather.FishAmountAndWeightView
import com.mobileprism.fishing.ui.home.views.*
import com.mobileprism.fishing.ui.viewmodels.UserCatchViewModel
import com.mobileprism.fishing.utils.Constants.MAX_PHOTOS
import com.mobileprism.fishing.utils.addPhoto
import com.mobileprism.fishing.utils.showToast
import java.util.*


sealed class BottomSheetCatchScreen() {
    object EditFishTypeAndWeightScreen : BottomSheetCatchScreen()
    object EditNoteScreen : BottomSheetCatchScreen()
    object EditPhotosScreen : BottomSheetCatchScreen()
    object EditWayOfFishingScreen : BottomSheetCatchScreen()
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class,
    ExperimentalPermissionsApi::class
)
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
                note = viewModel.catch.collectAsState().value.note,
                onSaveNote = { note -> viewModel.updateNote(note) },
                onCloseDialog = onCloseBottomSheet
            )

        }

        BottomSheetCatchScreen.EditPhotosScreen -> {
            AddPhotoDialog(
                photos = viewModel.catch.collectAsState().value.downloadPhotoLinks.map { it.toUri() },
                onSavePhotosClick = { newPhotos ->
                    viewModel.updateCatchPhotos(newPhotos)
//                    if (newPhotos.find { !it.toString().startsWith("http") } != null) {
//                        showInterstitialAd(
//                            context = context,
//                            onAdLoaded = { }
//                        )
//                    }
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

    LaunchedEffect(key1 = viewModel.catch.collectAsState().value) {
        viewModel.catch.value.let {
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

        FishingButtonFilled(
            modifier = Modifier.constrainAs(saveButton) {
                top.linkTo(amountAndWeight.bottom, 16.dp)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            text = stringResource(id = R.string.save),
            onClick = {
                viewModel.updateCatchInfo(
                    fishType = fishType.value,
                    fishAmount = fishAmount.value.toInt(),
                    fishWeight = fishWeight.value.toDouble()
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
            text = stringResource(id = R.string.cancel)
        ) { onCloseBottomSheet() }
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

    LaunchedEffect(key1 = viewModel.catch.collectAsState().value) {
        viewModel.catch.value.let {
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

        FishingButtonFilled(
            modifier = Modifier.constrainAs(saveButton) {
                top.linkTo(lureField.bottom, 16.dp)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            text = stringResource(id = R.string.save),
            onClick = {
                viewModel.updateWayOfFishing(
                    fishingRodType = rod.value,
                    fishingLure = lure.value,
                    fishingBait = bait.value
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
            text = stringResource(id = R.string.cancel)
        ) { onCloseBottomSheet() }

    }
}

@ExperimentalComposeUiApi
@Composable
fun EditNoteDialog(
    note: Note,
    onSaveNote: (Note) -> Unit,
    deleteOption: Boolean = false,
    onDeleteNote: (Note) -> Unit = {},
    onCloseDialog: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val onClose = {
        keyboardController?.hide()
        onCloseDialog()
    }

    val noteId = remember { mutableStateOf(note.id) }
    val noteTitle = remember { mutableStateOf(note.title) }
    val noteDescriptionState = remember { mutableStateOf(note.description) }
    val noteDateCreated = remember { mutableStateOf(note.dateCreated) }

    LaunchedEffect(note) {
        noteId.value = note.id
        noteTitle.value = note.title
        noteDescriptionState.value = note.description
        noteDateCreated.value = note.dateCreated
    }

    ConstraintLayout(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        val (title, editNote, saveButton, cancelButton, deleteButton) = createRefs()

        PrimaryText(
            modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top)
                absoluteLeft.linkTo(parent.absoluteLeft)
            },
            text = if (noteId.value.isEmpty()) stringResource(id = R.string.new_note)
            else stringResource(id = R.string.edit_note)
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

        FishingButtonFilled(
            modifier = Modifier.constrainAs(saveButton) {
                top.linkTo(editNote.bottom, 16.dp)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            text = stringResource(id = R.string.save),
            onClick = {
                onSaveNote(
                    Note(
                        id = noteId.value,
                        title = noteTitle.value,
                        description = noteDescriptionState.value,
                        dateCreated = Date().time
                    )
                )
                onClose()
            }
        )

        DefaultButton(
            modifier = Modifier.constrainAs(cancelButton) {
                top.linkTo(saveButton.top)
                bottom.linkTo(saveButton.bottom)
                absoluteRight.linkTo(saveButton.absoluteLeft, 8.dp)
            },
            text = stringResource(id = R.string.cancel)
        ) {
            onClose()
        }

        if (deleteOption) {
            IconButton(modifier = Modifier.constrainAs(deleteButton) {
                top.linkTo(saveButton.top)
                bottom.linkTo(saveButton.bottom)
                absoluteLeft.linkTo(parent.absoluteLeft)
            }, onClick = {
                onDeleteNote(
                    Note(
                        id = noteId.value,
                        title = noteTitle.value,
                        description = noteDescriptionState.value,
                        dateCreated = noteDateCreated.value
                    )
                )
                onClose()
            }) {
                Icon(Icons.Default.Delete, "Delete note")
            }
        }

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
                context.showToast(context.getString(R.string.max_photos_allowed))
            }
            tempDialogPhotosState.addAll(value)
        }

    LaunchedEffect(key1 = photos) {
        tempDialogPhotosState.clear()
        tempDialogPhotosState.addAll(photos)
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

        FishingButtonFilled(
            modifier = Modifier.constrainAs(saveButton) {
                top.linkTo(content.bottom, 16.dp)
                bottom.linkTo(parent.bottom, 32.dp)
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
            },
            text = stringResource(id = R.string.save),
            onClick = {
                if (tempDialogPhotosState.size > MAX_PHOTOS) {
                    context.showToast(context.getString(R.string.max_photos_allowed))
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

        DefaultButtonOutlinedOld(
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




package com.mobileprism.fishing.ui.home.catch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.new_catch.FishAmountAndWeightView
import com.mobileprism.fishing.ui.home.views.DefaultButton
import com.mobileprism.fishing.ui.home.views.DefaultButtonFilled
import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.ui.home.views.DefaultDialog
import com.mobileprism.fishing.ui.home.views.ItemPhoto
import com.mobileprism.fishing.ui.home.views.MaxCounterView
import com.mobileprism.fishing.ui.home.views.NoContentView
import com.mobileprism.fishing.ui.home.views.PrimaryText
import com.mobileprism.fishing.ui.home.views.SimpleOutlinedTextField
import com.mobileprism.fishing.ui.viewmodels.UserCatchViewModel
import com.mobileprism.fishing.ui.utils.MediaPickerLauncher
import com.mobileprism.fishing.utils.Constants.MAX_PHOTOS
import com.mobileprism.fishing.utils.ValidationUtils
import kotlin.time.Clock


sealed class BottomSheetCatchScreen() {
    object EditFishTypeAndWeightScreen : BottomSheetCatchScreen()
    object EditNoteScreen : BottomSheetCatchScreen()
    object EditPhotosScreen : BottomSheetCatchScreen()
    object EditWayOfFishingScreen : BottomSheetCatchScreen()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CatchModalBottomSheetContent(
    currentScreen: BottomSheetCatchScreen,
    viewModel: UserCatchViewModel,
    onCloseBottomSheet: () -> Unit,
    photoPicker: MediaPickerLauncher,
    onPickedPhotosHandlerChange: ((List<String>) -> Unit) -> Unit,
) {
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
                photos = viewModel.catch.collectAsState().value.downloadPhotoLinks,
                mediaPicker = photoPicker,
                onPickedPhotosHandlerChange = onPickedPhotosHandlerChange,
                onSavePhotosClick = { newPhotos ->
                    viewModel.updateCatchPhotos(newPhotos)
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PrimaryText(text = stringResource(Res.string.user_catch))

        SimpleOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textState = fishType,
            label = stringResource(Res.string.fish_species)
        )

        FishAmountAndWeightView(
            modifier = Modifier.fillMaxWidth(),
            amountState = fishAmount,
            weightState = fishWeight
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultButton(
                text = stringResource(Res.string.cancel)
            ) { onCloseBottomSheet() }
            Spacer(modifier = Modifier.width(8.dp))
            DefaultButtonFilled(
                text = stringResource(Res.string.save),
                onClick = {
                    viewModel.updateCatchInfo(
                        fishType = fishType.value,
                        fishAmount = fishAmount.value.toInt(),
                        fishWeight = fishWeight.value.toDouble()
                    )
                    onCloseBottomSheet()
                }
            )
        }
    }

}

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PrimaryText(text = stringResource(Res.string.way_of_fishing))

        SimpleOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textState = rod,
            label = stringResource(Res.string.fish_rod),
            singleLine = false
        )

        SimpleOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textState = bait,
            label = stringResource(Res.string.bait),
            singleLine = false
        )

        SimpleOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textState = lure,
            label = stringResource(Res.string.lure),
            singleLine = false
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultButton(
                text = stringResource(Res.string.cancel)
            ) { onCloseBottomSheet() }
            Spacer(modifier = Modifier.width(8.dp))
            DefaultButtonFilled(
                text = stringResource(Res.string.save),
                onClick = {
                    viewModel.updateWayOfFishing(
                        fishingRodType = rod.value,
                        fishingLure = lure.value,
                        fishingBait = bait.value
                    )
                    onCloseBottomSheet()
                }
            )
        }
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
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(note) {
        noteId.value = note.id
        noteTitle.value = note.title
        noteDescriptionState.value = note.description
        noteDateCreated.value = note.dateCreated
    }

    if (showDeleteConfirm) {
        DefaultDialog(
            primaryText = stringResource(Res.string.delete_note_dialog),
            secondaryText = stringResource(Res.string.sure_delete_note_dialog),
            onNegativeClick = { showDeleteConfirm = false },
            onPositiveClick = {
                onDeleteNote(
                    Note(
                        noteId.value,
                        noteTitle.value,
                        noteDescriptionState.value,
                        noteDateCreated.value
                    )
                )
                onClose()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    val isTitleValid = ValidationUtils.isValidNoteTitle(noteTitle.value)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PrimaryText(
            text = if (noteId.value.isEmpty()) stringResource(Res.string.new_note)
            else stringResource(Res.string.edit_note)
        )

        SimpleOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textState = noteTitle,
            label = stringResource(Res.string.title),
            singleLine = true,
            isError = !isTitleValid
        )

        SimpleOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textState = noteDescriptionState,
            label = stringResource(Res.string.note),
            singleLine = false,
            isError = noteDescriptionState.value.isBlank()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (deleteOption) {
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, stringResource(Res.string.delete))
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            DefaultButton(
                text = stringResource(Res.string.cancel)
            ) {
                onClose()
            }
            Spacer(modifier = Modifier.width(8.dp))
            DefaultButtonFilled(
                text = stringResource(Res.string.save),
                enabled = noteDescriptionState.value.isNotBlank() && isTitleValid,
                onClick = {
                    onSaveNote(
                        Note(
                            id = noteId.value,
                            title = noteTitle.value.trim(),
                            description = noteDescriptionState.value,
                            dateCreated = Clock.System.now().toEpochMilliseconds()
                        )
                    )
                    onClose()
                }
            )
        }
    }

}

@Composable
fun AddPhotoDialog(
    photos: List<String>,
    mediaPicker: MediaPickerLauncher,
    onPickedPhotosHandlerChange: ((List<String>) -> Unit) -> Unit,
    onSavePhotosClick: (List<String>) -> Unit,
    onCloseBottomSheet: () -> Unit
) {
    val tempDialogPhotosState = remember { mutableStateListOf<String>() }

    DisposableEffect(Unit) {
        onPickedPhotosHandlerChange { newPhotos ->
            if ((newPhotos.size + tempDialogPhotosState.size) > MAX_PHOTOS) {
                SnackbarManager.showMessage(Res.string.max_photos_allowed)
            } else {
                tempDialogPhotosState.addAll(newPhotos)
            }
        }

        onDispose {
            onPickedPhotosHandlerChange { _: List<String> -> }
        }
    }

    LaunchedEffect(key1 = photos) {
        tempDialogPhotosState.clear()
        tempDialogPhotosState.addAll(photos)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PrimaryText(text = stringResource(Res.string.photos))
            MaxCounterView(
                count = tempDialogPhotosState.size,
                maxCount = MAX_PHOTOS,
                icon = painterResource(Res.drawable.ic_baseline_photo_24)
            )
        }

        LazyRow(
            modifier = Modifier
                .defaultMinSize(minHeight = 120.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
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
                        text = stringResource(Res.string.no_photos_added),
                        icon = painterResource(Res.drawable.ic_no_photos)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultButtonOutlined(
                icon = painterResource(Res.drawable.ic_baseline_add_photo_alternate_24),
                text = stringResource(Res.string.gallery),
                onClick = { mediaPicker.launchGallery() }
            )
            Spacer(modifier = Modifier.width(4.dp))
            DefaultButtonOutlined(
                icon = painterResource(Res.drawable.ic_baseline_photo_camera_24),
                text = stringResource(Res.string.camera),
                onClick = { mediaPicker.launchCamera() }
            )
            Spacer(modifier = Modifier.weight(1f))
            DefaultButton(
                text = stringResource(Res.string.cancel),
                onClick = onCloseBottomSheet
            )
            Spacer(modifier = Modifier.width(8.dp))
            DefaultButtonFilled(
                text = stringResource(Res.string.save),
                onClick = {
                    if (tempDialogPhotosState.size > MAX_PHOTOS) {
                        SnackbarManager.showMessage(Res.string.max_photos_allowed)
                    } else {
                        onSavePhotosClick(tempDialogPhotosState.toList())
                        onCloseBottomSheet()
                    }
                }
            )
        }
    }
}

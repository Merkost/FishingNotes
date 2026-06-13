package com.mobileprism.fishing.ui.home.catch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.components.StepperField
import com.mobileprism.fishing.ui.home.views.AppIconButton
import com.mobileprism.fishing.ui.home.views.DefaultDialog
import com.mobileprism.fishing.ui.home.views.EditBottomSheetScaffold
import com.mobileprism.fishing.ui.home.views.ItemPhoto
import com.mobileprism.fishing.ui.home.views.MaxCounterView
import com.mobileprism.fishing.ui.home.views.NoContentView
import com.mobileprism.fishing.ui.home.views.SimpleOutlinedTextField
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.viewmodels.UserCatchViewModel
import com.mobileprism.fishing.ui.utils.MediaPickerLauncher
import com.mobileprism.fishing.utils.Constants.MAX_PHOTOS
import com.mobileprism.fishing.utils.ValidationUtils
import kotlin.time.Clock
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


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

    EditBottomSheetScaffold(
        title = stringResource(Res.string.user_catch),
        onCancel = onCloseBottomSheet,
        onSave = {
            viewModel.updateCatchInfo(
                fishType = fishType.value,
                fishAmount = fishAmount.value.toInt(),
                fishWeight = fishWeight.value.toDouble()
            )
            onCloseBottomSheet()
        }
    ) {
        SimpleOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textState = fishType,
            label = stringResource(Res.string.fish_species)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            StepperField(
                modifier = Modifier.weight(1f),
                value = fishAmount.value.toIntOrNull() ?: 0,
                onValueChange = { fishAmount.value = it.toString() },
                label = stringResource(Res.string.amount),
                suffix = stringResource(Res.string.pc),
                range = 0..ValidationUtils.MAX_FISH_AMOUNT
            )
            StepperField(
                modifier = Modifier.weight(1f),
                value = fishWeight.value.toDoubleOrNull() ?: 0.0,
                onValueChange = { fishWeight.value = it.toString() },
                label = stringResource(Res.string.weight),
                suffix = stringResource(Res.string.kg),
                range = 0.0..ValidationUtils.MAX_FISH_WEIGHT_KG
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

    EditBottomSheetScaffold(
        title = stringResource(Res.string.way_of_fishing),
        onCancel = onCloseBottomSheet,
        onSave = {
            viewModel.updateWayOfFishing(
                fishingRodType = rod.value,
                fishingLure = lure.value,
                fishingBait = bait.value
            )
            onCloseBottomSheet()
        }
    ) {
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

    EditBottomSheetScaffold(
        title = if (noteId.value.isEmpty()) stringResource(Res.string.new_note)
        else stringResource(Res.string.edit_note),
        onCancel = onClose,
        onSave = {
            onSaveNote(
                Note(
                    id = noteId.value,
                    title = noteTitle.value.trim(),
                    description = noteDescriptionState.value,
                    dateCreated = Clock.System.now().toEpochMilliseconds()
                )
            )
            onClose()
        },
        saveEnabled = noteDescriptionState.value.isNotBlank() && isTitleValid,
        leadingAction = if (deleteOption) {
            {
                AppIconButton(
                    onClick = { showDeleteConfirm = true },
                    icon = rememberVectorPainter(Icons.Default.Delete),
                    contentDescription = stringResource(Res.string.delete)
                )
            }
        } else null
    ) {
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

    EditBottomSheetScaffold(
        title = stringResource(Res.string.photos),
        onCancel = onCloseBottomSheet,
        onSave = {
            if (tempDialogPhotosState.size > MAX_PHOTOS) {
                SnackbarManager.showMessage(Res.string.max_photos_allowed)
            } else {
                onSavePhotosClick(tempDialogPhotosState.toList())
                onCloseBottomSheet()
            }
        },
        leadingAction = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconButton(
                    onClick = { mediaPicker.launchGallery() },
                    icon = painterResource(Res.drawable.ic_baseline_add_photo_alternate_24),
                    contentDescription = stringResource(Res.string.gallery)
                )
                AppIconButton(
                    onClick = { mediaPicker.launchCamera() },
                    icon = painterResource(Res.drawable.ic_baseline_photo_camera_24),
                    contentDescription = stringResource(Res.string.camera)
                )
            }
        }
    ) {
        MaxCounterView(
            count = tempDialogPhotosState.size,
            maxCount = MAX_PHOTOS,
            icon = painterResource(Res.drawable.ic_baseline_photo_24)
        )

        LazyRow(
            modifier = Modifier
                .defaultMinSize(minHeight = 120.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = Spacing.xs, horizontal = Spacing.xs),
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
    }
}

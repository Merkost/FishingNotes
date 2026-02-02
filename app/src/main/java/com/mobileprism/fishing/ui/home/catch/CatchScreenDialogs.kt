package com.mobileprism.fishing.ui.home.catch

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.ui.home.new_catch.FishAmountAndWeightView
import com.mobileprism.fishing.ui.home.views.DefaultButton
import com.mobileprism.fishing.ui.home.views.DefaultButtonFilled
import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.ui.home.views.ItemPhoto
import com.mobileprism.fishing.ui.home.views.MaxCounterView
import com.mobileprism.fishing.ui.home.views.NoContentView
import com.mobileprism.fishing.ui.home.views.PrimaryText
import com.mobileprism.fishing.ui.home.views.SimpleOutlinedTextField
import com.mobileprism.fishing.ui.viewmodels.UserCatchViewModel
import com.mobileprism.fishing.utils.Constants.MAX_PHOTOS
import com.mobileprism.fishing.utils.showToast
import java.util.Date


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
                    // FIXME: Add ads
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
        PrimaryText(text = stringResource(id = R.string.user_catch))

        SimpleOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textState = fishType,
            label = stringResource(id = R.string.fish_species)
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
                text = stringResource(id = R.string.cancel)
            ) { onCloseBottomSheet() }
            Spacer(modifier = Modifier.width(8.dp))
            DefaultButtonFilled(
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
        PrimaryText(text = stringResource(id = R.string.way_of_fishing))

        SimpleOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textState = rod,
            label = stringResource(id = R.string.fish_rod),
            singleLine = false
        )

        SimpleOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textState = bait,
            label = stringResource(id = R.string.bait),
            singleLine = false
        )

        SimpleOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textState = lure,
            label = stringResource(id = R.string.lure),
            singleLine = false
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultButton(
                text = stringResource(id = R.string.cancel)
            ) { onCloseBottomSheet() }
            Spacer(modifier = Modifier.width(8.dp))
            DefaultButtonFilled(
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

    LaunchedEffect(note) {
        noteId.value = note.id
        noteTitle.value = note.title
        noteDescriptionState.value = note.description
        noteDateCreated.value = note.dateCreated
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PrimaryText(
            text = if (noteId.value.isEmpty()) stringResource(id = R.string.new_note)
            else stringResource(id = R.string.edit_note)
        )

        SimpleOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textState = noteDescriptionState,
            label = stringResource(id = R.string.note),
            singleLine = false
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (deleteOption) {
                IconButton(onClick = {
                    onDeleteNote(
                        Note(
                            noteId.value,
                            noteTitle.value,
                            noteDescriptionState.value,
                            noteDateCreated.value
                        )
                    )
                    onClose()
                }) {
                    Icon(Icons.Default.Delete, "Delete note")
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            DefaultButton(
                text = stringResource(id = R.string.cancel)
            ) {
                onClose()
            }
            Spacer(modifier = Modifier.width(8.dp))
            DefaultButtonFilled(
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
        }
    }

}

@Composable
fun AddPhotoDialog(
    photos: List<Uri>,
    onSavePhotosClick: (List<Uri>) -> Unit,
    onCloseBottomSheet: () -> Unit
) {
    val context = LocalContext.current
    val tempDialogPhotosState = remember { mutableStateListOf<Uri>() }

    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { value ->
            if ((value.size + tempDialogPhotosState.size) > MAX_PHOTOS) {
                showToast(context, context.getString(R.string.max_photos_allowed))
            }
            tempDialogPhotosState.addAll(value)
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
            PrimaryText(text = stringResource(id = R.string.photos))
            MaxCounterView(
                count = tempDialogPhotosState.size,
                maxCount = MAX_PHOTOS,
                icon = painterResource(id = R.drawable.ic_baseline_photo_24)
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
                        text = stringResource(id = R.string.no_photos_added),
                        icon = painterResource(id = R.drawable.ic_no_photos)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultButtonOutlined(
                icon = painterResource(id = R.drawable.ic_baseline_add_photo_alternate_24),
                text = stringResource(id = R.string.add),
                onClick = {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            DefaultButton(
                text = stringResource(id = R.string.cancel),
                onClick = onCloseBottomSheet
            )
            Spacer(modifier = Modifier.width(8.dp))
            DefaultButtonFilled(
                text = stringResource(id = R.string.save),
                onClick = {
                    if (tempDialogPhotosState.size > MAX_PHOTOS) {
                        showToast(context, context.getString(R.string.max_photos_allowed))
                    } else {
                        onSavePhotosClick(tempDialogPhotosState)
                        onCloseBottomSheet()
                    }
                }
            )
        }
    }
}


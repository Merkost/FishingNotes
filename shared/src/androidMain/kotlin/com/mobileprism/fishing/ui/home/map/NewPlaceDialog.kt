package com.mobileprism.fishing.ui.home.map

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.raw.RawMapMarker
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.home.views.DefaultButton
import com.mobileprism.fishing.ui.home.views.DefaultButtonFilled
import com.mobileprism.fishing.ui.home.views.MyCard
import com.mobileprism.fishing.ui.theme.Shapes
import com.mobileprism.fishing.ui.theme.secondaryFigmaColor
import com.mobileprism.fishing.utils.ValidationUtils
import com.mobileprism.fishing.ui.utils.ColorPicker
import com.mobileprism.fishing.viewmodels.MapViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NewPlaceDialog(
    dialogState: Boolean,
    onDismiss: () -> Unit,
) {
    if (dialogState) {
        Dialog(onDismissRequest = { onDismiss() }) {
            val viewModel: MapViewModel = koinViewModel()
            val currentCameraPosition by viewModel.currentCameraPosition.collectAsState()
            val uiState by viewModel.addNewMarkerState.collectAsState()
            val noNamePlace = stringResource(Res.string.no_name_place)

            LaunchedEffect(uiState) {
                when (uiState) {
                    UiState.Success -> {
                        onDismiss()
                        viewModel.resetAddNewMarkerState()
                        SnackbarManager.showMessage(Res.string.add_place_success)
                    }
                    UiState.Error -> {
                        viewModel.resetAddNewMarkerState()
                        SnackbarManager.showMessage(Res.string.add_new_place_error)
                    }
                    else -> {}
                }
            }

            MyCard(shape = Shapes.large, modifier = Modifier.wrapContentHeight().fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(4.dp)
                ) {
                    val placeTileViewNameState by viewModel.placeTileViewNameState.collectAsState()

                    val titleValue = remember { mutableStateOf("") }
                    val descriptionValue = remember { mutableStateOf("") }
                    val markerColor = remember { mutableStateOf(pickerColors[0].value.hashCode()) }

                    SetPlaceNameResultListener(placeTileViewNameState.geocoderResult) {
                        titleValue.value = it
                    }

                    Text(
                        text = stringResource(Res.string.new_place),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(top = 8.dp, start = 4.dp)
                    )

                    val (textField1, textField2) = remember { FocusRequester.createRefs() }
                    val keyboardController = LocalSoftwareKeyboardController.current

                    OutlinedTextField(
                        value = titleValue.value,
                        onValueChange = {
                            if (it.length <= ValidationUtils.MAX_PLACE_NAME_LENGTH) {
                                titleValue.value = it
                            }
                        },
                        label = { Text(text = stringResource(Res.string.title)) },
                        singleLine = true,
                        supportingText = {
                            Text(
                                stringResource(
                                    Res.string.char_counter,
                                    titleValue.value.length,
                                    ValidationUtils.MAX_PLACE_NAME_LENGTH
                                )
                            )
                        },
                        visualTransformation = VisualTransformation.None,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { textField2.requestFocus() }
                        ),
                        trailingIcon = {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = titleValue.value.isNotEmpty(),
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                IconButton(onClick = { titleValue.value = "" }) {
                                    Icon(Icons.Default.Close, Icons.Default.Delete.name)
                                }
                            }
                        },
                        modifier = Modifier
                            .focusRequester(textField1)
                            .padding(top = 8.dp)
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = descriptionValue.value,
                        onValueChange = {
                            descriptionValue.value = it
                        },
                        label = { Text(text = stringResource(Res.string.description)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        ),
                        modifier = Modifier
                            .focusRequester(textField2)
                            .padding(top = 2.dp)
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                    )

                    val (selectedColor, onColorSelected) = remember { mutableStateOf(pickerColors[0]) }

                    Row(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(8.dp)
                                .requiredSize(40.dp)
                                .clip(CircleShape)
                        )
                        {
                            Icon(
                                painter = painterResource(Res.drawable.ic_baseline_location_on_24),
                                contentDescription = stringResource(Res.string.marker_icon),
                                tint = selectedColor,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 2.dp)
                            )
                        }
                        ColorPicker(
                            pickerColors,
                            selectedColor,
                            { color ->
                                color?.let {
                                    onColorSelected(it)
                                    markerColor.value = it.value.hashCode()
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp, bottom = 14.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        DefaultButton(
                            modifier = Modifier.padding(end = 8.dp),
                            text = stringResource(Res.string.cancel),
                            onClick = {
                                viewModel.cancelAddNewMarker()
                                onDismiss()
                            }
                        )

                        DefaultButtonFilled(
                            text = stringResource(Res.string.save),
                            enabled = uiState !is UiState.InProgress,
                            onClick = {
                                val trimmedTitle = titleValue.value.trim()
                                viewModel.addNewMarker(
                                    RawMapMarker(
                                        title = when (trimmedTitle.isEmpty()) {
                                            true -> noNamePlace
                                            false -> trimmedTitle
                                        },
                                        description = descriptionValue.value,
                                        latitude = currentCameraPosition.first.latitude,
                                        longitude = currentCameraPosition.first.longitude,
                                        markerColor = markerColor.value
                                    )
                                )
                            }
                        )
                    }

                    DisposableEffect(Unit) {
                        textField1.requestFocus()
                        onDispose { }
                    }
                }
            }
        }
    }
}

val pickerColors = listOf(
    Color(0xFFEC407A),
    Color(0xFFAB47BC),
    Color(0xFF7E57C2),
    Color(0xFF5C6BC0),
    Color(0xFF42A5F5),
    Color(0xFF29B6F6),
    Color(0xFF26C6DA),
    Color(0xFF26A69A),
    Color(0xFF66BB6A),
    Color(0xFF9CCC65),
    Color(0xFFD4E157),
    Color(0xFFFFEE58),
    Color(0xFFFFCA28),
    Color(0xFFFFA726),
    Color(0xFFFF7043)
)


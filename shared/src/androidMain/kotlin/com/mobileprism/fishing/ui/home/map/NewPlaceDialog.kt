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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import com.mobileprism.fishing.R
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
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NewPlaceDialog(
    dialogState: Boolean,
    onDismiss: () -> Unit,
) {
    if (dialogState) {
        Dialog(onDismissRequest = { onDismiss() }) {
            val context = LocalContext.current
            val viewModel: MapViewModel = koinViewModel()
            val currentCameraPosition by viewModel.currentCameraPosition.collectAsState()
            val uiState by viewModel.addNewMarkerState.collectAsState()

            LaunchedEffect(uiState) {
                when (uiState) {
                    UiState.Success -> {
                        onDismiss()
                        viewModel.resetAddNewMarkerState()
                        SnackbarManager.showMessage(R.string.add_place_success)
                    }
                    UiState.Error -> {
                        viewModel.resetAddNewMarkerState()
                        SnackbarManager.showMessage(R.string.add_new_place_error)
                    }
                    else -> {}
                }
            }

            MyCard(shape = Shapes.large, modifier = Modifier.wrapContentHeight().fillMaxWidth()) {
                ConstraintLayout(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(4.dp)
                ) {
                    val (progress, name, locationIcon, title, description, saveButton, cancelButton) = createRefs()

                    val placeTileViewNameState by viewModel.placeTileViewNameState.collectAsState()

                    val titleValue = remember { mutableStateOf("") }
                    val descriptionValue = remember { mutableStateOf("") }
                    val markerColor = remember { mutableStateOf(Color(0xFFEC407A).hashCode()) }

                    SetPlaceNameResultListener(placeTileViewNameState.geocoderResult) {
                        titleValue.value = it
                    }

                    Text(
                        text = stringResource(R.string.new_place),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.constrainAs(name) {
                            top.linkTo(parent.top, 8.dp)
                            absoluteLeft.linkTo(parent.absoluteLeft, 4.dp)
                            absoluteRight.linkTo(parent.absoluteRight)
                        }
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
                        label = { Text(text = stringResource(R.string.title)) },
                        singleLine = true,
                        supportingText = {
                            Text(
                                stringResource(
                                    R.string.char_counter,
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
                            .constrainAs(title) {
                                top.linkTo(name.bottom, 8.dp)
                                absoluteLeft.linkTo(parent.absoluteLeft)
                                absoluteRight.linkTo(parent.absoluteRight)
                            }
                            .focusRequester(textField1)
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = descriptionValue.value,
                        onValueChange = {
                            descriptionValue.value = it
                        },
                        label = { Text(text = stringResource(R.string.description)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        ),
                        modifier = Modifier
                            .constrainAs(description) {
                                top.linkTo(title.bottom, 2.dp)
                                absoluteLeft.linkTo(parent.absoluteLeft)
                                absoluteRight.linkTo(parent.absoluteRight)
                            }
                            .focusRequester(textField2)
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                    )

                    val (selectedColor, onColorSelected) = remember { mutableStateOf(pickerColors[0]) }

                    Row(modifier = Modifier
                        .constrainAs(locationIcon) {
                            top.linkTo(description.bottom, 6.dp)
                            absoluteLeft.linkTo(parent.absoluteLeft)
                            absoluteRight.linkTo(parent.absoluteRight)
                        }
                        .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(8.dp)
                                .requiredSize(40.dp)
                                .clip(CircleShape)
                        )
                        {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                                contentDescription = stringResource(R.string.marker_icon),
                                tint = selectedColor ?: secondaryFigmaColor,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 2.dp)
                            )
                        }
                        ColorPicker(
                            pickerColors,
                            selectedColor,
                            (onColorSelected as (Color?) -> Unit).apply {
                                markerColor.value = selectedColor.value.hashCode()
                            },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }

                    DefaultButton(
                        modifier = Modifier.constrainAs(cancelButton) {
                            absoluteRight.linkTo(saveButton.absoluteLeft, 8.dp)
                            top.linkTo(saveButton.top)
                            bottom.linkTo(saveButton.bottom)
                        },
                        text = stringResource(id = R.string.cancel),
                        onClick = {
                            viewModel.cancelAddNewMarker()
                            onDismiss()
                        }
                    )

                    DefaultButtonFilled(
                        modifier = Modifier.constrainAs(saveButton) {
                            absoluteRight.linkTo(parent.absoluteRight, 8.dp)
                            top.linkTo(locationIcon.bottom, 14.dp)
                            bottom.linkTo(parent.bottom, 14.dp)
                        },
                        text = stringResource(id = R.string.save),
                        enabled = uiState !is UiState.InProgress,
                        onClick = {
                            val trimmedTitle = titleValue.value.trim()
                            viewModel.addNewMarker(
                                RawMapMarker(
                                    title = when (trimmedTitle.isEmpty()) {
                                        true -> context.resources.getString(R.string.no_name_place)
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
    //null,
    //Color(0xFF000000),
    //Color(0xFFFFFFFF),
    //Color(0xFFFAFAFA),
    //Color(0x80FF4444),
    //Color(0xFFEF5350),
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


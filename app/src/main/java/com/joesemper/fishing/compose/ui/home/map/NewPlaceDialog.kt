package com.joesemper.fishing.compose.ui.home.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
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
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.android.libraries.maps.model.LatLng
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.MyCard
import com.joesemper.fishing.compose.ui.home.SnackbarManager
import com.joesemper.fishing.compose.ui.home.UiState
import com.joesemper.fishing.compose.ui.theme.Shapes
import com.joesemper.fishing.compose.ui.theme.secondaryFigmaColor
import com.joesemper.fishing.compose.viewmodels.MapViewModel
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@ExperimentalComposeUiApi
@Composable
fun NewPlaceDialog(
    currentCameraPosition: MutableState<Pair<LatLng, Float>>,
    dialogState: MutableState<Boolean>,
    chosenPlace: MutableState<String?>,
) {
    val viewModel = get<MapViewModel>()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    MyCard(shape = Shapes.large, modifier = Modifier.wrapContentHeight()) {
        ConstraintLayout(
            modifier = Modifier
                .wrapContentHeight()
                .padding(4.dp)
        ) {
            val (progress, name, locationIcon, title, description, saveButton, cancelButton) = createRefs()

            uiState?.let {
                when (it) {
                    UiState.InProgress -> {
                        Surface(color = Color.Gray, modifier = Modifier
                            .constrainAs(progress) {
                                top.linkTo(parent.top)
                                absoluteLeft.linkTo(parent.absoluteLeft)
                                absoluteRight.linkTo(parent.absoluteRight)
                                bottom.linkTo(parent.bottom)
                            }
                            .size(100.dp)) {
                            FishLoading(modifier = Modifier.size(150.dp))
                        }
                    }
                    UiState.Success -> {
                        coroutineScope.launch {
                            dialogState.value = false
                            SnackbarManager.showMessage(R.string.add_place_success)
                        }
                    }
                    else -> {
                    }
                }
            }

            val descriptionValue = remember { mutableStateOf("") }
            val titleValue = remember { mutableStateOf(/*chosenPlace.value ?:*/ "") }
            LaunchedEffect(chosenPlace.value) {
                chosenPlace.value?.let {
                    titleValue.value = it
                }
            }

            Icon(painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                contentDescription = stringResource(R.string.marker_icon),
                tint = secondaryFigmaColor,
                modifier = Modifier.constrainAs(locationIcon) {
                    absoluteRight.linkTo(name.absoluteLeft, 14.dp)
                    top.linkTo(name.top)
                    bottom.linkTo(name.bottom)
                })

            Text(
                text = stringResource(R.string.new_place),
                style = MaterialTheme.typography.h6,
                modifier = Modifier.constrainAs(name) {
                    top.linkTo(parent.top, 16.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft, 4.dp)
                    absoluteRight.linkTo(parent.absoluteRight)
                }
            )

            val (textField1, textField2) = remember { FocusRequester.createRefs() }
            val keyboardController = LocalSoftwareKeyboardController.current

            OutlinedTextField(
                value = titleValue.value,
                onValueChange = { titleValue.value = it },
                label = { Text(text = stringResource(R.string.title)) },
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { textField2.requestFocus() }
                ),
                modifier = Modifier
                    .constrainAs(title) {
                        top.linkTo(name.bottom, 12.dp)
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

            OutlinedButton(modifier = Modifier.constrainAs(cancelButton) {
                absoluteRight.linkTo(parent.absoluteRight, 4.dp)
                top.linkTo(description.bottom, 14.dp)
                bottom.linkTo(parent.bottom, 14.dp)
            },
                shape = RoundedCornerShape(24.dp), onClick = {
                    coroutineScope.launch {
                        dialogState.value = false
                    }
                }) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(id = R.string.cancel),
                        //modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Button(modifier = Modifier.constrainAs(saveButton) {
                absoluteRight.linkTo(cancelButton.absoluteLeft, 8.dp)
                top.linkTo(cancelButton.top)
                bottom.linkTo(cancelButton.bottom)
            }, shape = RoundedCornerShape(24.dp), onClick = {
                viewModel.addNewMarker(
                    RawMapMarker(
                        titleValue.value,
                        descriptionValue.value,
                        currentCameraPosition.component1().first.latitude,
                        currentCameraPosition.component1().first.longitude
                    )
                )
            }) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(id = R.string.save),
                        //modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            DisposableEffect(Unit) {
                textField1.requestFocus()
                onDispose { }
            }
        }

    }
}


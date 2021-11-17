package com.joesemper.fishing.compose.ui.home.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
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
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.android.libraries.maps.model.LatLng
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.MyCard
import com.joesemper.fishing.compose.ui.home.SnackbarManager
import com.joesemper.fishing.compose.ui.home.UiState
import com.joesemper.fishing.compose.ui.theme.Shapes
import com.joesemper.fishing.compose.ui.theme.secondaryFigmaColor
import com.joesemper.fishing.compose.ui.utils.ColorPicker
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
            val markerColor = remember { mutableStateOf(Color(0xFFEC407A).hashCode()) }
            LaunchedEffect(chosenPlace.value) {
                chosenPlace.value?.let {
                    titleValue.value = it
                }
            }

            Text(
                text = stringResource(R.string.new_place),
                style = MaterialTheme.typography.h6,
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
                }.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(8.dp)
                        .requiredSize(40.dp).clip(CircleShape))
                {
                    Image(painterResource(R.drawable.transparent), stringResource(R.string.transparent),)
                    Icon(painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                        contentDescription = stringResource(R.string.marker_icon),
                        tint = selectedColor ?: secondaryFigmaColor,
                    modifier = Modifier.fillMaxSize().padding(top = 2.dp))
                }
                ColorPicker(
                    pickerColors,
                    selectedColor,
                    (onColorSelected as (Color?) -> Unit).apply { markerColor.value = selectedColor.value.hashCode() },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            OutlinedButton(modifier = Modifier.constrainAs(cancelButton) {
                absoluteRight.linkTo(parent.absoluteRight, 4.dp)
                top.linkTo(locationIcon.bottom, 14.dp)
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
                        currentCameraPosition.component1().first.longitude,
                        markerColor.value
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


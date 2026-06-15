package com.mobileprism.fishing.ui.home.map

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.domain.entity.raw.RawMapMarker
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.home.views.AppButton
import com.mobileprism.fishing.ui.home.views.AppButtonStyle
import com.mobileprism.fishing.ui.utils.ColorGrid
import com.mobileprism.fishing.utils.ValidationUtils
import com.mobileprism.fishing.viewmodels.MapViewModel
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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

@Composable
fun NewPlaceBottomSheetContent(
    onDismiss: () -> Unit,
) {
    val viewModel: MapViewModel = koinViewModel()
    val currentCameraPosition = remember { viewModel.currentCameraPosition.value }
    val uiState by viewModel.addNewMarkerState.collectAsState()
    val placeTileState = remember { viewModel.placeTileViewNameState.value }

    val noNamePlace = stringResource(Res.string.no_name_place)
    val unnamedPlace = stringResource(Res.string.unnamed_place)
    val cantRecognizePlace = stringResource(Res.string.cant_recognize_place)
    val searchingText = stringResource(Res.string.searching)

    val geocoderResult = placeTileState.geocoderResult

    val placeholderText = when (geocoderResult) {
        is GeocoderResult.Success -> geocoderResult.placeName
        GeocoderResult.NoNamePlace -> unnamedPlace
        GeocoderResult.Failed -> cantRecognizePlace
        GeocoderResult.InProgress -> searchingText
    }

    val fallbackName = when (geocoderResult) {
        is GeocoderResult.Success -> geocoderResult.placeName
        else -> noNamePlace
    }

    val saveEnabled = uiState !is UiState.InProgress

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

    var titleValue by remember { mutableStateOf("") }
    var descriptionValue by remember { mutableStateOf("") }
    var descriptionExpanded by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(pickerColors[0]) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.new_place),
                style = FishingTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(Res.drawable.ic_baseline_location_on_24),
                contentDescription = null,
                tint = selectedColor,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = titleValue,
            onValueChange = {
                if (it.length <= ValidationUtils.MAX_PLACE_NAME_LENGTH) {
                    titleValue = it
                }
            },
            placeholder = { Text(placeholderText) },
            label = { Text(stringResource(Res.string.title)) },
            singleLine = true,
            supportingText = {
                Text(
                    stringResource(
                        Res.string.char_counter,
                        titleValue.length,
                        ValidationUtils.MAX_PLACE_NAME_LENGTH
                    )
                )
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = titleValue.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(onClick = { titleValue = "" }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = if (descriptionExpanded) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            ),
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))

        AnimatedVisibility(
            visible = !descriptionExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            TextButton(onClick = { descriptionExpanded = true }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(Res.string.description),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = descriptionExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            OutlinedTextField(
                value = descriptionValue,
                onValueChange = { descriptionValue = it },
                label = { Text(stringResource(Res.string.description)) },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.color),
            style = FishingTheme.typography.labelMedium,
            color = FishingTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        ColorGrid(
            colors = pickerColors,
            selectedColor = selectedColor,
            onColorSelected = { selectedColor = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (uiState is UiState.InProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(12.dp))
            }
            AppButton(
                text = stringResource(Res.string.save),
                style = AppButtonStyle.Filled,
                enabled = saveEnabled,
                onClick = {
                    val trimmedTitle = titleValue.trim()
                    viewModel.addNewMarker(
                        RawMapMarker(
                            title = trimmedTitle.ifEmpty { fallbackName },
                            description = descriptionValue,
                            latitude = currentCameraPosition.latitude,
                            longitude = currentCameraPosition.longitude,
                            markerColor = selectedColor.toArgb()
                        )
                    )
                }
            )
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

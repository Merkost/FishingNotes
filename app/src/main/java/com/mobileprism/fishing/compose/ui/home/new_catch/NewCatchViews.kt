package com.mobileprism.fishing.compose.ui.home.new_catch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.mobileprism.fishing.R
import com.mobileprism.fishing.compose.ui.Arguments
import com.mobileprism.fishing.compose.ui.MainDestinations
import com.mobileprism.fishing.compose.ui.home.SnackbarManager
import com.mobileprism.fishing.compose.ui.home.views.*
import com.mobileprism.fishing.domain.NewCatchViewModel
import com.mobileprism.fishing.domain.viewstates.ErrorType
import com.mobileprism.fishing.domain.viewstates.RetrofitWrapper
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.mappers.getAllWeatherIcons
import com.mobileprism.fishing.utils.Constants.WIND_ROTATION
import com.mobileprism.fishing.utils.roundTo
import com.mobileprism.fishing.utils.showToast
import com.mobileprism.fishing.utils.time.toDate
import com.mobileprism.fishing.utils.time.toTime
import org.koin.androidx.compose.getViewModel

@ExperimentalComposeUiApi
@Composable
fun Places(viewModel: NewCatchViewModel, isNull: Boolean, navController: NavController) {
    val context = LocalContext.current

    val changePlaceError = stringResource(R.string.another_place_in_new_catch)
    val marker by rememberSaveable { viewModel.marker }

    var isDropMenuOpen by rememberSaveable { mutableStateOf(false) }

    var textFieldValue by rememberSaveable {
        mutableStateOf(
            marker?.title ?: ""
        )
    }

    val suggestions = remember { mutableStateListOf<UserMapMarker>() }

    viewModel.markersListState.value.let {
        when (it) {
            is NewCatchPlacesState.NotReceived -> {
            }
            is NewCatchPlacesState.Received -> {
                if (it.locations.isEmpty()) {
                    NewCatchNoPlaceDialog(navController)
                } else {
                    suggestions.apply {
                        clear()
                        addAll(it.locations)
                    }
                }
            }
        }
    }

    val filteredList by rememberSaveable { mutableStateOf(suggestions.toMutableList()) }
    if (textFieldValue == "") searchFor("", suggestions, filteredList)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Spacer(modifier = Modifier.padding(1.dp))

        SubtitleWithIcon(
            modifier = Modifier.align(Alignment.Start),
            icon = R.drawable.ic_baseline_location_on_24,
            text = stringResource(id = R.string.location)
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                readOnly = !isNull,
                singleLine = true,
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    if (suggestions.isNotEmpty()) {
                        searchFor(textFieldValue, suggestions, filteredList)
                        isDropMenuOpen = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        isDropMenuOpen = it.isFocused
                    },
                label = { Text(text = stringResource(R.string.place)) },
                trailingIcon = {

                    if (isNull) {
                        if (textFieldValue.isNotEmpty()) {
                            IconButton(onClick = {
                                textFieldValue = ""; isDropMenuOpen = true
                            }) {
                                Icon(
                                    Icons.Default.Close,
                                    "",
                                    tint = MaterialTheme.colors.primary
                                )
                            }

                        } else {
                            IconButton(onClick = {
                                if (!isDropMenuOpen) isDropMenuOpen = true
                            }) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    "",
                                    tint = MaterialTheme.colors.primary
                                )
                            }
                        }
                    } else IconButton(onClick = {
                        showToast(
                            context,
                            changePlaceError
                        )

                    }) {
                        Icon(
                            Icons.Default.Lock,
                            stringResource(R.string.locked),
                            tint = MaterialTheme.colors.primary,
                        )
                    }
                },
                isError = !isThatPlaceInList(
                    textFieldValue,
                    suggestions
                ).apply { viewModel.noErrors.value = this },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            DropdownMenu(
                expanded = isDropMenuOpen, //suggestions.isNotEmpty(),
                onDismissRequest = {
                    if (isDropMenuOpen) isDropMenuOpen = false
                },
                properties = PopupProperties(focusable = false)
            ) {
                filteredList.forEach { suggestion ->
                    DropdownMenuItem(
                        onClick = {
                            textFieldValue = suggestion.title
                            viewModel.marker.value = suggestion
                            isDropMenuOpen = false
                        }) {
                        Text(text = suggestion.title)
                    }
                }
            }
        }
    }
}

@Composable
fun FishSpecies(
    modifier: Modifier = Modifier,
    name: State<String>,
    onNameChange: (String) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        OutlinedTextField(
            value = name.value,
            onValueChange = { onNameChange(it) },
            label = { Text(stringResource(R.string.fish_species)) },
            modifier = Modifier.fillMaxWidth(),
            isError = name.value.isBlank(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.size(2.dp))
        Text(
            stringResource(R.string.required), fontSize = 12.sp, modifier = Modifier.align(
                Alignment.End
            )
        )
    }
}


@Composable
fun Fishing(
    rod: MutableState<String>,
    bite: MutableState<String>,
    lure: MutableState<String>
) {
    Column {
        SubtitleWithIcon(
            modifier = Modifier.align(Alignment.Start),
            icon = R.drawable.ic_fishing_rod,
            text = stringResource(id = R.string.way_of_fishing)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SimpleOutlinedTextField(textState = rod, label = stringResource(R.string.fish_rod))
            SimpleOutlinedTextField(textState = bite, label = stringResource(R.string.bait))
            SimpleOutlinedTextField(textState = lure, label = stringResource(R.string.lure))
        }

    }
}

@Composable
fun FishAndWeight(fishState: MutableState<String>, weightState: MutableState<String>) {
    val viewModel: NewCatchViewModel = getViewModel()
    Column {

        SubtitleWithIcon(
            modifier = Modifier.align(Alignment.Start),
            icon = R.drawable.ic_fish,
            text = stringResource(id = R.string.fish_catch)
        )

//        FishSpecies(viewModel.fishType)

        SimpleOutlinedTextField(
            textState = viewModel.description,
            label = stringResource(R.string.note)
        )
        Spacer(modifier = Modifier.size(2.dp))
        FishAmountAndWeightView(amountState = fishState, weightState = weightState)

    }
}

@Composable
fun FishAmountAndWeightView(
    modifier: Modifier = Modifier,
    amountState: MutableState<String>,
    weightState: MutableState<String>
) {
    Row(modifier = modifier) {
        Column(Modifier.weight(1F)) {
            OutlinedTextField(
                value = amountState.value,
                onValueChange = {
                    if (it.isEmpty()) amountState.value = it
                    else {
                        amountState.value = when (it.toIntOrNull()) {
                            null -> amountState.value //old value
                            else -> it   //new value
                        }
                    }
                },
                isError = amountState.value.isEmpty(),
                label = { Text(text = stringResource(R.string.amount)) },
                trailingIcon = { Text(stringResource(R.string.pc)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.size(6.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        if (amountState.value.toInt() >= 1 && amountState.value.isNotBlank())
                            amountState.value = ((amountState.value.toInt() - 1).toString())
                    },
                    Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_minus),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = ""
                    )
                }
                Spacer(modifier = Modifier.size(6.dp))
                OutlinedButton(
                    onClick = {
                        if (amountState.value.isEmpty()) amountState.value = 1.toString()
                        else amountState.value =
                            ((amountState.value.toInt() + 1).toString())
                    },
                    Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_plus),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = ""
                    )
                }
            }

        }
        Spacer(modifier = Modifier.size(6.dp))
        Column(Modifier.weight(1F)) {
            OutlinedTextField(
                value = weightState.value,
                onValueChange = {
                    if (it.isEmpty()) weightState.value = it
                    else {
                        weightState.value = when (it.toDoubleOrNull()) {
                            null -> weightState.value //old value
                            else -> it   //new value
                        }
                    }
                },
                label = { Text(text = stringResource(R.string.weight)) },
                trailingIcon = {
                    Text(stringResource(R.string.kg))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.size(6.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        if (weightState.value.toDouble() >= 0.1 && weightState.value.isNotBlank())
                            weightState.value =
                                ((weightState.value.toDouble() - 0.1).roundTo(1).toString())
                    },
                    Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_minus),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = ""
                    )
                }
                Spacer(modifier = Modifier.size(6.dp))
                OutlinedButton(
                    onClick = {
                        if (weightState.value.isEmpty()) weightState.value =
                            0.1f.roundTo(1).toString()
                        else weightState.value =
                            ((weightState.value.toDouble() + 0.1).roundTo(1).toString())
                    },
                    Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_plus),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = ""
                    )
                }
            }
        }
    }
}

@Composable
fun NewCatchWeatherItem(viewModel: NewCatchViewModel) {

    val weather by viewModel.weather.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween.also { Arrangement.Center },
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            SubtitleWithIcon(
                icon = R.drawable.weather_sunny,
                text = stringResource(id = R.string.weather)
            )
            if (weather != null) {
                IconButton(onClick = { viewModel.getWeather() }) {
                    Icon(Icons.Default.Refresh, "", tint = MaterialTheme.colors.primary)
                }
            } else Spacer(modifier = Modifier.size(LocalViewConfiguration.current.minimumTouchTargetSize))
        }

        AnimatedVisibility(weatherState is RetrofitWrapper.Success) {
            WeatherLayout(weather, viewModel)
        }

        AnimatedVisibility(weatherState is RetrofitWrapper.Loading) {
            if (viewModel.marker.value == null) {
                NoContentView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    text = stringResource(R.string.select_place_for_weather),
                    icon = painterResource(id = R.drawable.ic_baseline_location_on_24)
                )
            } else {
                WeatherLayoutLoading()
            }

        }

        AnimatedVisibility(weatherState is RetrofitWrapper.Error) {
            if (weatherState is RetrofitWrapper.Error) {
                when ((weatherState as RetrofitWrapper.Error).errorType) {
                    is ErrorType.NetworkError -> {
                        SecondaryText(
                            modifier = Modifier.padding(8.dp),
                            text = stringResource(R.string.no_internet)
                        )
                    }
                    is ErrorType.OtherError -> {
                        SecondaryText(
                            modifier = Modifier.padding(8.dp),
                            text = "Произошла ошибка!"
                        )
                    }
                }
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun PickWeatherIconDialog(onIconSelected: (Int) -> Unit, onDismiss: () -> Unit) {
    DefaultDialog(
        stringResource(R.string.choose_weather_icon),
        content = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                mainAxisAlignment = FlowMainAxisAlignment.Center,
                crossAxisAlignment = FlowCrossAxisAlignment.Center,
            ) {
                getAllWeatherIcons().distinct().forEach { iconResource ->
                    WeatherIconItem(iconResource) { onIconSelected(iconResource) }
                }
            }
        }, onDismiss = onDismiss
    )
}

@ExperimentalComposeUiApi
@Composable
fun PickWindDirDialog(onDirectionSelected: (Float) -> Unit, onDismiss: () -> Unit) {
    DefaultDialog(
        primaryText = stringResource(R.string.choose_wind_direction),
        content = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                mainAxisAlignment = FlowMainAxisAlignment.Center,
                crossAxisAlignment = FlowCrossAxisAlignment.Center,
            ) {
                (0..7).forEach {
                    WindIconItem(
                        rotation = it * WIND_ROTATION,
                        onIconSelected = { onDirectionSelected(it * WIND_ROTATION) }
                    )
                }
            }
        }, onDismiss = onDismiss
    )
}

@Composable
fun DateAndTime(
    date: MutableState<Long>,
) {
    val viewModel: NewCatchViewModel = getViewModel()
    val dateSetState = remember { mutableStateOf(false) }
    val timeSetState = remember { mutableStateOf(false) }
    val context = LocalContext.current

//    if (dateSetState.value) DatePicker(date, dateSetState, context)
//    if (timeSetState.value) TimePicker(date, timeSetState, context)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = date.value.toDate(),
            onValueChange = {},
            label = { Text(text = stringResource(R.string.date)) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    if (viewModel.noErrors.value) dateSetState.value = true
                    else {
                        SnackbarManager.showMessage(R.string.choose_place_first)
                    }
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_baseline_event_24),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = stringResource(R.string.date)
                    )
                }

            })
        OutlinedTextField(
            value = date.value.toTime(),
            onValueChange = {},
            label = { Text(text = stringResource(R.string.time)) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    if (viewModel.noErrors.value) timeSetState.value = true
                    else {
                        SnackbarManager.showMessage(R.string.choose_place_first)
                    }
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_baseline_access_time_24),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = stringResource(R.string.time)
                    )
                }

            })
    }
}

@ExperimentalComposeUiApi
@Composable
fun NewCatchNoPlaceDialog(
    navController: NavController
) {
    DefaultDialog(
        primaryText = stringResource(R.string.no_places_added),
        secondaryText = stringResource(R.string.add_location_dialog),
        negativeButtonText = stringResource(id = R.string.cancel),
        onNegativeClick = { navController.popBackStack() },
        positiveButtonText = stringResource(id = R.string.add),
        onPositiveClick = { onAddNewPlaceClick(navController) },
        onDismiss = { navController.popBackStack() },
        content = {
            LottieNoPlaces(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
    )
}

@Composable
fun LottieNoPlaces(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_loaction))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(
        composition,
        progress,
        modifier = modifier
    )
}

private fun onAddNewPlaceClick(navController: NavController) {
    val addNewPlace = true
    navController.navigate("${MainDestinations.HOME_ROUTE}/${MainDestinations.MAP_ROUTE}?${Arguments.MAP_NEW_PLACE}=${addNewPlace}")
}

@ExperimentalComposeUiApi
@Composable
fun NewCatchPlaceSelectView(
    modifier: Modifier = Modifier,
    marker: State<UserMapMarker?>,
    markersList: State<NewCatchPlacesState>,
    isLocationLocked: Boolean,
    onNewPlaceSelected: (UserMapMarker) -> Unit,
    onInputError: (Boolean) -> Unit
) {
    val context = LocalContext.current

    var isDropMenuOpen by rememberSaveable { mutableStateOf(false) }

    var textFieldValue by rememberSaveable {
        mutableStateOf(marker.value?.title ?: "")
    }

    val suggestions = remember { mutableStateListOf<UserMapMarker>() }

    LaunchedEffect(key1 = markersList.value) {
        markersList.value.let { state ->
            when (state) {
                is NewCatchPlacesState.NotReceived -> {
                }
                is NewCatchPlacesState.Received -> {
                    if (state.locations.isEmpty()) {
//                        NewCatchNoPlaceDialog(navController)
                    } else {
                        suggestions.apply {
                            clear()
                            addAll(state.locations)
                        }
                    }
                }
            }
        }
    }

    val filteredList by rememberSaveable { mutableStateOf(suggestions.toMutableList()) }
    if (textFieldValue == "") searchFor("", suggestions, filteredList)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (isLocationLocked) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true,
                label = { Text(text = stringResource(R.string.place)) },
                value = marker.value?.title ?: "",
                onValueChange = { },
                trailingIcon = {
                    IconButton(
                        onClick = { SnackbarManager.showMessage(R.string.another_place_in_new_catch) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = stringResource(R.string.locked),
                            tint = MaterialTheme.colors.primary,
                        )
                    }
                }
            )
        } else {
            OutlinedTextField(
                readOnly = false,
                singleLine = true,
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    if (suggestions.isNotEmpty()) {
                        searchFor(textFieldValue, suggestions, filteredList)
                        isDropMenuOpen = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        isDropMenuOpen = it.isFocused
                    },
                label = { Text(text = stringResource(R.string.place)) },
                trailingIcon = {
                    if (textFieldValue.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                textFieldValue = ""
                                isDropMenuOpen = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = Icons.Default.Close.name,
                                tint = MaterialTheme.colors.primary
                            )
                        }

                    } else {
                        IconButton(
                            onClick = {
                                if (!isDropMenuOpen) isDropMenuOpen = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = Icons.Default.KeyboardArrowDown.name,
                                tint = MaterialTheme.colors.primary
                            )
                        }
                    }
                },
                isError = !isThatPlaceInList(textFieldValue, suggestions).apply { onInputError(this) }
            )
        }

        DropdownMenu(
            modifier = Modifier
                .wrapContentWidth(),
            expanded = isDropMenuOpen && suggestions.isNotEmpty(),
            onDismissRequest = {
                if (isDropMenuOpen) isDropMenuOpen = false
            },
            properties = PopupProperties(focusable = false)
        ) {
            filteredList.forEach { suggestion ->
                DropdownMenuItem(
                    onClick = {
                        textFieldValue = suggestion.title
                        onNewPlaceSelected(suggestion)
                        isDropMenuOpen = false
                    }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = Icons.Default.LocationOn.name,
                            tint = Color(suggestion.markerColor)
                        )
                        Text(text = suggestion.title)
                    }

                }
            }
        }
    }
}

@Composable
fun DateAndTimeItem(
    modifier: Modifier = Modifier,
    date: State<Long>,
    onDateChange: (Long) -> Unit,
) {
    val viewModel: NewCatchViewModel = getViewModel()
    val dateSetState = remember { mutableStateOf(false) }
    val timeSetState = remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (dateSetState.value) {
        DatePicker(dateSetState = dateSetState, context = context, onDateChange = onDateChange)
    }

    if (timeSetState.value) {
        TimePicker(timeSetState = timeSetState, context = context, onTimeChange = onDateChange)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = date.value.toDate(),
            onValueChange = {},
            label = { Text(text = stringResource(R.string.date)) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    if (viewModel.noErrors.value) dateSetState.value = true
                    else {
                        SnackbarManager.showMessage(R.string.choose_place_first)
                    }
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_baseline_event_24),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = stringResource(R.string.date)
                    )
                }

            })
        OutlinedTextField(
            value = date.value.toTime(),
            onValueChange = {},
            label = { Text(text = stringResource(R.string.time)) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    if (viewModel.noErrors.value) timeSetState.value = true
                    else {
                        SnackbarManager.showMessage(R.string.choose_place_first)
                    }
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_baseline_access_time_24),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = stringResource(R.string.time)
                    )
                }

            })
    }
}

@Composable
fun FishAmountAndWeightViewItem(
    modifier: Modifier = Modifier,
    amountState: State<Int>,
    weightState: State<Double>,
    onAmountChange: (Int) -> Unit,
    onWeightChange: (Double) -> Unit
) {
    Row(modifier = modifier) {
        Column(Modifier.weight(1F)) {
            OutlinedTextField(
                value = amountState.value.toString(),
                onValueChange = {
                    if (it.isEmpty()) onAmountChange(it.toInt())
                    else {
                        when (it.toIntOrNull()) {
                            null -> onAmountChange(amountState.value) //old value
                            else -> onAmountChange(it.toInt())   //new value
                        }
                    }
                },
                isError = amountState.value.toString().isEmpty(),
                label = { Text(text = stringResource(R.string.amount)) },
                trailingIcon = { Text(stringResource(R.string.pc)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.size(6.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        if (amountState.value >= 1 && amountState.value.toString().isNotBlank()) {
                            onAmountChange(amountState.value - 1)
                        }

                    },
                    Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_minus),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = ""
                    )
                }
                Spacer(modifier = Modifier.size(6.dp))
                OutlinedButton(
                    onClick = {
                        if (amountState.value.toString().isEmpty()) onAmountChange(1)
                        else onAmountChange((amountState.value + 1))
                    },
                    Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_plus),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = ""
                    )
                }
            }
        }
        Spacer(modifier = Modifier.size(6.dp))
        Column(Modifier.weight(1F)) {
            OutlinedTextField(
                value = weightState.value.toString(),
                onValueChange = {
                    if (it.isEmpty()) onWeightChange(it.toDouble())
                    else {
                        when (it.toDoubleOrNull()) {
                            null -> onWeightChange(weightState.value) //old value
                            else -> onWeightChange(it.toDouble())   //new value
                        }
                    }
                },
                label = { Text(text = stringResource(R.string.weight)) },
                trailingIcon = {
                    Text(stringResource(R.string.kg))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.size(6.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        if (weightState.value >= 0.1 && weightState.value.toString().isNotBlank()) {
                            onWeightChange((weightState.value - 0.1).roundTo(1))
                        }
                    },
                    Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_minus),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = ""
                    )
                }
                Spacer(modifier = Modifier.size(6.dp))
                OutlinedButton(
                    onClick = {
                        if (weightState.value.toString().isEmpty()) onWeightChange(
                            0.1.roundTo(1)
                        )
                        else onWeightChange(
                            (weightState.value + 0.1).roundTo(1)
                        )
                    },
                    Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_plus),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = ""
                    )
                }
            }
        }
    }
}

@Composable
fun WayOfFishingView(
    modifier: Modifier = Modifier,
    rodState: State<String>,
    biteState: State<String>,
    lureState: State<String>,
    onRodChange: (String) -> Unit,
    onBiteChange: (String) -> Unit,
    onLureChange: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = rodState.value,
            onValueChange = { onRodChange(it) },
            label = { Text(text = stringResource(R.string.fish_rod)) }
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = biteState.value,
            onValueChange = { onBiteChange(it) },
            label = { Text(text = stringResource(R.string.bait)) }
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = lureState.value,
            onValueChange = { onLureChange(it) },
            label = { Text(text = stringResource(R.string.lure)) }
        )
    }
}

@Composable
fun NewCatchWeatherView(viewModel: NewCatchViewModel) {

    val weather by viewModel.weather.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween.also { Arrangement.Center },
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            SubtitleWithIcon(
                icon = R.drawable.weather_sunny,
                text = stringResource(id = R.string.weather)
            )
            if (weather != null) {
                IconButton(onClick = { viewModel.getWeather() }) {
                    Icon(Icons.Default.Refresh, "", tint = MaterialTheme.colors.primary)
                }
            } else Spacer(modifier = Modifier.size(LocalViewConfiguration.current.minimumTouchTargetSize))
        }

        AnimatedVisibility(weatherState is RetrofitWrapper.Success) {
            WeatherLayout(weather, viewModel)
        }

        AnimatedVisibility(weatherState is RetrofitWrapper.Loading) {
            if (viewModel.marker.value == null) {
                NoContentView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    text = stringResource(R.string.select_place_for_weather),
                    icon = painterResource(id = R.drawable.ic_baseline_location_on_24)
                )
            } else {
                WeatherLayoutLoading()
            }

        }

        AnimatedVisibility(weatherState is RetrofitWrapper.Error) {
            if (weatherState is RetrofitWrapper.Error) {
                when ((weatherState as RetrofitWrapper.Error).errorType) {
                    is ErrorType.NetworkError -> {
                        SecondaryText(
                            modifier = Modifier.padding(8.dp),
                            text = stringResource(R.string.no_internet)
                        )
                    }
                    is ErrorType.OtherError -> {
                        SecondaryText(
                            modifier = Modifier.padding(8.dp),
                            text = "Произошла ошибка!"
                        )
                    }
                }
            }
        }
    }
}

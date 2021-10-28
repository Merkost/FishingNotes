package com.joesemper.fishing.compose.ui.home

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import coil.annotation.ExperimentalCoilApi
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.notes.ItemPhoto
import com.joesemper.fishing.domain.NewCatchViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.mappers.getMoonIconByPhase
import com.joesemper.fishing.model.mappers.getWeatherIconByName
import com.joesemper.fishing.ui.theme.primaryFigmaColor
import com.joesemper.fishing.ui.theme.secondaryFigmaTextColor
import com.joesemper.fishing.utils.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import org.koin.androidx.compose.getViewModel
import java.util.*


private val dateAndTime = Calendar.getInstance()
private var isNull: Boolean = true

object Constants {
    private const val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 111
    const val ITEM_ADD_PHOTO = "ITEM_ADD_PHOTO"
    const val ITEM_PHOTO = "ITEM_PHOTO"
}

@ExperimentalPermissionsApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalCoilApi
@Composable
fun NewCatchScreen(upPress: () -> Unit, place: UserMapMarker) {

    val viewModel: NewCatchViewModel = getViewModel()
    val context = LocalContext.current
    val notAllFieldsFilled = stringResource(R.string.not_all_fields_are_filled)

    viewModel.date.value = dateAndTime.timeInMillis

    if (place.id.isNotEmpty()) {
        viewModel.marker.value = place; isNull = false
    }

    LaunchedEffect(key1 = viewModel.marker.value, key2 = viewModel.date.value) {
        viewModel.marker.value?.let {
            if (getDateByMilliseconds(viewModel.date.value) != getDateByMilliseconds(Date().time)) {
                viewModel.getHistoricalWeather()?.collect {
                    viewModel.weather.value = it
                }
            } else {
                viewModel.getWeather()?.collect {
                    viewModel.weather.value = it
                }
            }
        }

    }

    Scaffold(
        modifier = Modifier.navigationBarsWithImePadding(),
        topBar = { NewCatchAppBar(upPress) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (viewModel.isInputCorrect())
                        viewModel.createNewUserCatch() else showToast(
                        context,
                        notAllFieldsFilled
                    )
                }) {
                Icon(Icons.Filled.Done, stringResource(R.string.create), tint = Color.White)
            }
        }
    ) {
        SubscribeToProgress(viewModel.uiState, upPress)
        val scrollState = rememberScrollState()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(30.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(state = scrollState, enabled = true),
        ) {
            Places(stringResource(R.string.place), viewModel)  //Выпадающий список мест
            FishAndWeight(viewModel.fishAmount, viewModel.weight)
            Fishing(viewModel.rod, viewModel.bite, viewModel.lure)
            DateAndTime(viewModel.date)
            NewCatchWeather(viewModel)
            Photos(
                { clicked -> { } },
                { deleted -> viewModel.deletePhoto(deleted) })
            Spacer(modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun SubscribeToProgress(vmuiState: StateFlow<BaseViewState>, upPress: () -> Unit) {
    val errorDialog = rememberSaveable { mutableStateOf(false) }

    val uiState by vmuiState.collectAsState()
    when (uiState) {
        is BaseViewState.Success<*> -> {
            if ((uiState as BaseViewState.Success<*>).data != null) {
                Toast.makeText(
                    LocalContext.current,
                    "Ваш улов успешно добавлен!",
                    Toast.LENGTH_SHORT
                ).show()
                upPress()
            }
        }
        is BaseViewState.Loading -> {
            LoadingDialog()
        }
        is BaseViewState.Error -> {
            ErrorDialog(errorDialog)
            errorDialog.value = true
            Toast.makeText(
                LocalContext.current,
                "Error: ${(uiState as BaseViewState.Error).error.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

//TODO("AutoCompleteTextView for places textField")
@Composable
private fun Places(label: String, viewModel: NewCatchViewModel) {
    val context = LocalContext.current

    val changePlaceError = stringResource(R.string.Another_place_in_new_catch)
    val marker by rememberSaveable { viewModel.marker }
    var textFieldValue by rememberSaveable {
        mutableStateOf(
            marker?.title ?: ""
        )
    }
    var isDropMenuOpen by rememberSaveable { mutableStateOf(false) }
    val suggestions by viewModel.getAllUserMarkersList().collectAsState(listOf())
    val filteredList by rememberSaveable { mutableStateOf(suggestions.toMutableList()) }
    if (textFieldValue == "") searchFor("", suggestions, filteredList)


    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Spacer(modifier = Modifier.padding(1.dp))

        SubtitleWithIcon(
            modifier = Modifier.align(Alignment.Start),
            icon = R.drawable.ic_baseline_location_on_24,
            text = stringResource(id = R.string.location)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
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
                label = { Text(text = label) },
                trailingIcon = {
                    if (isNull) {
                        if (textFieldValue.isNotEmpty()) {
                            Icon(
                                Icons.Default.Close,
                                "",
                                modifier = Modifier.clickable {
                                    textFieldValue = ""; isDropMenuOpen = true
                                },
                                tint = primaryFigmaColor
                            )
                        } else {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                "",
                                modifier = Modifier.clickable {
                                    if (!isDropMenuOpen) isDropMenuOpen = true
                                },
                                tint = primaryFigmaColor
                            )
                        }
                    } else Icon(
                        Icons.Default.Lock,
                        stringResource(R.string.locked),
                        tint = primaryFigmaColor,
                        modifier = Modifier.clickable {
                            showToast(
                                context,
                                changePlaceError
                            )

                        })
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
                // This line here will accomplish what you want
                properties = PopupProperties(focusable = false),
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

private fun isThatPlaceInList(
    textFieldValue: String,
    suggestions: List<UserMapMarker>
): Boolean {
    suggestions.forEach {
        if (it.title == textFieldValue) return true
    }
    return false
}

private fun searchFor(
    what: String,
    where: List<UserMapMarker>,
    filteredList: MutableList<UserMapMarker>
) {
    filteredList.clear()
    where.forEach {
        if (it.title.contains(what, ignoreCase = true)) {
            filteredList.add(it)
        }
    }
}

@Composable
fun FishSpecies(name: MutableState<String>) {
    Column {
        OutlinedTextField(
            value = name.value,
            onValueChange = { name.value = it },
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
            SimpleOutlinedTextField(rod, stringResource(R.string.fish_rod))
            SimpleOutlinedTextField(bite, stringResource(R.string.bait))
            SimpleOutlinedTextField(lure, stringResource(R.string.lure))
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

        FishSpecies(viewModel.fishType)

        SimpleOutlinedTextField(viewModel.description, stringResource(R.string.note))
        Spacer(modifier = Modifier.size(2.dp))
        Row {
            Column(Modifier.weight(1F)) {
                OutlinedTextField(
                    value = fishState.value,
                    onValueChange = {
                        if (it.isEmpty()) fishState.value = it
                        else {
                            fishState.value = when (it.toIntOrNull()) {
                                null -> fishState.value //old value
                                else -> it   //new value
                            }
                        }
                    },
                    isError = fishState.value.isEmpty(),
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
                            if (fishState.value.toInt() >= 1 && fishState.value.isNotBlank())
                                fishState.value = ((fishState.value.toInt() - 1).toString())
                        },
                        Modifier
                            .weight(1F)
                            .fillMaxHeight()
                            .align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_minus),
                            tint = secondaryFigmaTextColor,
                            contentDescription = ""
                        )
                    }
                    Spacer(modifier = Modifier.size(6.dp))
                    OutlinedButton(
                        onClick = {
                            if (fishState.value.isEmpty()) fishState.value = 1.toString()
                            else fishState.value =
                                ((fishState.value.toInt() + 1).toString())
                        },
                        Modifier
                            .weight(1F)
                            .fillMaxHeight()
                            .align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_plus),
                            tint = secondaryFigmaTextColor,
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
                            .fillMaxHeight()
                            .align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_minus),
                            tint = secondaryFigmaTextColor,
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
                            .fillMaxHeight()
                            .align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_plus),
                            tint = secondaryFigmaTextColor,
                            contentDescription = ""
                        )
                    }
                }
            }
        }

    }
}

@ExperimentalPermissionsApi
@ExperimentalAnimationApi
@Composable
fun Photos(
    clickedPhoto: (Uri) -> Unit,
    deletedPhoto: (Uri) -> Unit
) {
    val viewModel: NewCatchViewModel = getViewModel()
    val photos = remember { viewModel.images }
    Column {

        SubtitleWithIcon(
            modifier = Modifier.align(Alignment.Start),
            icon = R.drawable.ic_baseline_image_24,
            text = stringResource(id = R.string.photos)
        )

        LazyRow(modifier = Modifier.fillMaxSize()) {
            item { ItemAddPhoto() }
            items(items = photos) {
                ItemPhoto(
                    photo = it,
                    clickedPhoto = clickedPhoto,
                    deletedPhoto = deletedPhoto
                )
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
    }
}

@ExperimentalPermissionsApi
@Composable
fun ItemAddPhoto() {
    val viewModel: NewCatchViewModel = getViewModel()
    val permissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)
    val addPhotoState = rememberSaveable { mutableStateOf(false) }
    val choosePhotoLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { value ->
            value.forEach {
                viewModel.addPhoto(it)
            }
        }

    Card(
        modifier = Modifier
            .size(100.dp)
            .padding(4.dp)
            .fillMaxSize()
            .clip(RoundedCornerShape(5.dp))
            .clickable { addPhotoState.value = true },
        elevation = 8.dp,
        border = BorderStroke(1.dp, primaryFigmaColor)
    ) {

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painterResource(R.drawable.ic_baseline_add_photo_alternate_24), //Or we can use Icons.Default.Add
                contentDescription = Constants.ITEM_ADD_PHOTO,
                tint = secondaryFigmaTextColor,
                modifier = Modifier.size(48.dp)
            )
            SecondaryText(text = stringResource(id = R.string.add_photo))
        }


    }
    if (addPhotoState.value) {
        LaunchedEffect(addPhotoState) {
            permissionState.launchPermissionRequest()
        }
        addPhoto(permissionState, addPhotoState, choosePhotoLauncher)
    }
}

@ExperimentalPermissionsApi
private fun addPhoto(
    permissionState: PermissionState,
    addPhotoState: MutableState<Boolean>,
    choosePhotoLauncher: ManagedActivityResultLauncher<Array<String>, MutableList<Uri>>
) {
    when {
        permissionState.hasPermission -> {
            choosePhotoLauncher.launch(arrayOf("image/*"))
            addPhotoState.value = false
        }
    }
}

@Composable
fun NewCatchWeather(viewModel: NewCatchViewModel) {

    val weather = viewModel.weather.value

    Column(modifier = Modifier.fillMaxWidth()) {

        SubtitleWithIcon(
            modifier = Modifier.align(Alignment.Start),
            icon = R.drawable.weather_sunny,
            text = stringResource(id = R.string.weather)
        )

        if (weather != null && weather.hourly.isNotEmpty()) {

            val currentMoonPhase = remember {
                weather.daily.first().moonPhase
            }

            viewModel.moonPhase.value = calcMoonPhase(
                currentMoonPhase,
                Date().time / MILLISECONDS_IN_SECOND,
                weather.hourly.first().date
            )

            val hour by remember(dateAndTime.timeInMillis) {
                mutableStateOf(getHoursByMilliseconds(dateAndTime.timeInMillis).toInt())
            }

            Crossfade(targetState = weather) { weatherForecast ->
                Column() {
                    Spacer(Modifier.size(8.dp))
                    OutlinedTextField(
                        readOnly = true,
                        value = weatherForecast.hourly[hour].weather.first().description,
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(32.dp),
                                painter = painterResource(
                                    id = getWeatherIconByName(weatherForecast.hourly.first().weather.first().icon)
                                ),
                                contentDescription = "",
                                tint = secondaryFigmaTextColor
                            )
                        },
                        onValueChange = { },
                        label = { Text(text = stringResource(id = R.string.weather)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.size(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            readOnly = true,
                            value = weatherForecast.hourly[hour].temperature.toString(),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_thermometer),
                                    contentDescription = "",
                                    tint = secondaryFigmaTextColor
                                )
                            },
                            trailingIcon = {
                                Text(text = stringResource(R.string.celsius))
                            },
                            onValueChange = { },
                            label = { Text(text = stringResource(R.string.temperature)) },
                            modifier = Modifier.weight(1f, true),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.padding(4.dp))

                        OutlinedTextField(
                            readOnly = true,
                            value = hPaToMmHg(weatherForecast.hourly[hour].pressure).toString(),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_gauge),
                                    contentDescription = "",
                                    tint = secondaryFigmaTextColor
                                )
                            },
                            trailingIcon = {
                                Text(
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    text = stringResource(R.string.pressure_units)
                                )
                            },
                            onValueChange = { },
                            label = { Text(text = stringResource(R.string.pressure)) },
                            modifier = Modifier.weight(1f, true),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(Modifier.size(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            readOnly = true,
                            value = weatherForecast.hourly[hour].windSpeed.toString(),
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier.rotate(weatherForecast.hourly[hour].windDeg.toFloat()),
                                    painter = painterResource(id = R.drawable.ic_arrow_up),
                                    contentDescription = "",
                                    tint = secondaryFigmaTextColor,
                                )
                            },
                            trailingIcon = {
                                Text(text = stringResource(R.string.wind_speed_units))
                            },
                            onValueChange = { },
                            label = { Text(text = stringResource(R.string.wind)) },
                            modifier = Modifier.weight(1f, true),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.padding(4.dp))

                        OutlinedTextField(
                            readOnly = true,
                            value = (viewModel.moonPhase.value * 100).toInt().toString(),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(
                                        id = getMoonIconByPhase(viewModel.moonPhase.value)
                                    ),
                                    contentDescription = "",
                                    tint = secondaryFigmaTextColor
                                )
                            },
                            onValueChange = { },
                            trailingIcon = {
                                Text(text = stringResource(R.string.percent))
                            },
                            label = { Text(text = stringResource(R.string.moon_phase)) },
                            modifier = Modifier.weight(1f, true),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )
                    }
                }

            }
        } else {
            SecondaryText(
                modifier = Modifier.padding(vertical = 8.dp),
                text = "Select place to load weather"
            )
        }
    }
}

@Composable
fun DateAndTime(date: MutableState<Long>) {
    val dateSetState = remember { mutableStateOf(false) }
    val timeSetState = remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (dateSetState.value) DatePicker(date, dateSetState, context)
    if (timeSetState.value) TimePicker(date, timeSetState, context)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = getDateByMilliseconds(date.value),
            onValueChange = {},
            label = { Text(text = stringResource(R.string.date)) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                Icon(painter = painterResource(R.drawable.ic_baseline_event_24),
                    tint = primaryFigmaColor,
                    contentDescription = stringResource(R.string.date),
                    modifier = Modifier.clickable { dateSetState.value = true })
            })
        OutlinedTextField(
            value = getTimeByMilliseconds(date.value),
            onValueChange = {},
            label = { Text(text = stringResource(R.string.time)) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                Icon(painter = painterResource(R.drawable.ic_baseline_access_time_24),
                    tint = primaryFigmaColor,
                    contentDescription = stringResource(R.string.time),
                    modifier = Modifier.clickable {
                        timeSetState.value = true
                    })
            })
    }
}

@Composable
fun NewCatchAppBar(upPress: () -> Unit) {
    DefaultAppBar(
        onNavClick = upPress,
        title = stringResource(R.string.new_catch)
    )
}

@Composable
fun ErrorDialog(errorDialog: MutableState<Boolean>) {
    val viewModel: NewCatchViewModel = getViewModel()
    val context = LocalContext.current
    AlertDialog(
        title = { Text("Произошла ошибка!") },
        text = { Text("Не удалось загрузить фотографии. Проверьте интернет соединение и попробуйте еще раз.") },
        onDismissRequest = { errorDialog.value = false },
        confirmButton = {
            OutlinedButton(
                onClick = { viewModel.createNewUserCatch() },
                content = { Text(stringResource(R.string.Try_again)) })
        }, dismissButton = {
            OutlinedButton(
                onClick = { errorDialog.value = false },
                content = { Text(stringResource(R.string.Cancel)) })
        }
    )
}

@Composable
fun LoadingDialog() {
    Dialog(onDismissRequest = {}) {
        Card(modifier = Modifier.wrapContentSize()) {
            Column(modifier = Modifier.wrapContentSize()) {
                PrimaryText(
                    modifier = Modifier
                        .align(alignment = Alignment.Start)
                        .padding(8.dp),
                    text = stringResource(R.string.saving_new_catch)
                )
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.fish_loading))
                LottieAnimation(
                    modifier = Modifier.size(128.dp),
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    isPlaying = true
                )
                OutlinedButton(
                    modifier = Modifier
                        .align(alignment = Alignment.End)
                        .padding(8.dp),
                    onClick = { },
                    content = { Text(stringResource(R.string.Cancel)) }
                )
            }
        }

    }
}

@Composable
private fun TimePicker(
    date: MutableState<Long>,
    timeSetState: MutableState<Boolean>,
    context: Context
) {
    TimePickerDialog(
        context,
        TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            dateAndTime.set(Calendar.MINUTE, minute)
            date.value = dateAndTime.timeInMillis
        },
        dateAndTime.get(Calendar.HOUR_OF_DAY),
        dateAndTime.get(Calendar.MINUTE), true
    ).show()
    timeSetState.value = false
}

@Composable
private fun DatePicker(
    date: MutableState<Long>,
    dateSetState: MutableState<Boolean>,
    context: Context
) {
    DatePickerDialog(
        context,
        { _, year, monthOfYear, dayOfMonth ->
            dateAndTime.set(Calendar.YEAR, year)
            dateAndTime.set(Calendar.MONTH, monthOfYear)
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            date.value = dateAndTime.timeInMillis
        },
        dateAndTime.get(Calendar.YEAR),
        dateAndTime.get(Calendar.MONTH),
        dateAndTime.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.maxDate = Date().time
        datePicker.minDate = Date().time - (MILLISECONDS_IN_DAY * 5)
        show()
    }
    dateSetState.value = false
}


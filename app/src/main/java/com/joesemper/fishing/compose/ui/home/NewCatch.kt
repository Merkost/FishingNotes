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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.PopupProperties
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.notes.ItemPhoto
import com.joesemper.fishing.compose.ui.home.notes.WeatherLayout
import com.joesemper.fishing.compose.ui.home.notes.WeatherLayoutLoading
import com.joesemper.fishing.domain.NewCatchViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.domain.viewstates.ErrorType
import com.joesemper.fishing.domain.viewstates.RetrofitWrapper
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.mappers.getAllWeatherIcons
import com.joesemper.fishing.utils.MILLISECONDS_IN_DAY
import com.joesemper.fishing.utils.network.ConnectionState
import com.joesemper.fishing.utils.network.currentConnectivityState
import com.joesemper.fishing.utils.network.observeConnectivityAsFlow
import com.joesemper.fishing.utils.roundTo
import com.joesemper.fishing.utils.showToast
import com.joesemper.fishing.utils.time.toDate
import com.joesemper.fishing.utils.time.toTime
import kotlinx.coroutines.flow.StateFlow
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.viewModel
import java.util.*

private val dateAndTime = Calendar.getInstance()

object Constants {
    const val MAX_PHOTOS: Int = 5
    const val ITEM_ADD_PHOTO = "ITEM_ADD_PHOTO"
    const val ITEM_PHOTO = "ITEM_PHOTO"
}

@ExperimentalPermissionsApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalCoilApi
@Composable
fun NewCatchScreen(upPress: () -> Unit, place: UserMapMarker) {


    val viewModel: NewCatchViewModel by viewModel()
    val context = LocalContext.current
    val connectionState by context.observeConnectivityAsFlow()
        .collectAsState(initial = context.currentConnectivityState)

    SubscribeToProgress(viewModel.uiState, upPress)
    val scrollState = rememberScrollState()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(BottomSheetValue.Expanded)
    )

    viewModel.date.value = dateAndTime.timeInMillis
    var isNull by remember {
        mutableStateOf(true)
    }

    if (place.id.isNotEmpty()) {
        viewModel.marker.value = place; isNull = false
    }

    LaunchedEffect(key1 = viewModel.marker.value, key2 = viewModel.date.value, connectionState) {
        viewModel.marker.value?.let {
            if (viewModel.date.value.toDate() != Date().time.toDate()) {
                viewModel.getHistoricalWeather()
            } else {
                viewModel.getWeather()
            }
        }
    }

    DisposableEffect(key1 = dateAndTime) {
        onDispose {
            dateAndTime.timeInMillis = Date().time
        }
    }

    BottomSheetScaffold(
/*        scaffoldState = bottomSheetScaffoldState,*/
        modifier = Modifier.navigationBarsWithImePadding(),
        topBar = { NewCatchAppBar(upPress) },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(bottom = 16.dp),
                onClick = {
                    if (viewModel.isInputCorrect()) viewModel.createNewUserCatch()
                    else SnackbarManager.showMessage(R.string.not_all_fields_are_filled)
                }) {
                Icon(
                    Icons.Filled.Done,
                    stringResource(R.string.create),
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        },
        sheetContent = { BannerAdvertView() },
        sheetShape = RectangleShape,
        sheetGesturesEnabled = false
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(30.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = scrollState, enabled = true)
                    .padding(horizontal = 16.dp, vertical = 12.dp)

            ) {

                Places(viewModel, isNull)  //Выпадающий список мест
                FishAndWeight(viewModel.fishAmount, viewModel.weight)
                Fishing(viewModel.rod, viewModel.bite, viewModel.lure)
                DateAndTime(viewModel.date)
                NewCatchWeatherItem(viewModel, connectionState)
                Photos(
                    { clicked -> { } },
                    { deleted -> viewModel.deletePhoto(deleted) }, connectionState
                )
                Spacer(modifier = Modifier.padding(16.dp))
            }
            BannerAdvertView(/*modifier = Modifier.padding(4.dp)*/)
        }

    }
}

@Composable
fun BannerAdvertView(modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val isInEditMode = LocalInspectionMode.current
    if (isInEditMode) {
        Text(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.Red)
                .padding(horizontal = 2.dp, vertical = 6.dp),
            textAlign = TextAlign.Center,
            color = Color.White,
            text = "Advert Here",
        )
    } else {
        AndroidView(
            modifier = modifier.fillMaxWidth().wrapContentHeight(),
            factory = { context ->
                AdView(context).apply {
                    adSize = AdSize
                        .getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                            context, configuration.screenWidthDp)
                    adUnitId = context.getString(R.string.new_catch_admob_banner_id)
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
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
                    stringResource(R.string.catch_added_successfully),
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

@Composable
private fun Places(viewModel: NewCatchViewModel, isNull: Boolean) {
    val context = LocalContext.current

    val changePlaceError = stringResource(R.string.Another_place_in_new_catch)
    val marker by rememberSaveable { viewModel.marker }

    var isDropMenuOpen by rememberSaveable { mutableStateOf(false) }

    var textFieldValue by rememberSaveable {
        mutableStateOf(
            marker?.title ?: ""
        )
    }
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

@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalPermissionsApi
@ExperimentalAnimationApi
@Composable
fun Photos(
    clickedPhoto: (Uri) -> Unit,
    deletedPhoto: (Uri) -> Unit,
    connectionState: ConnectionState
) {
    val viewModel: NewCatchViewModel = getViewModel()
    val photos = remember { viewModel.images }
    Column {

        SubtitleWithIcon(
            modifier = Modifier.align(Alignment.Start),
            icon = R.drawable.ic_baseline_image_24,
            text = stringResource(id = R.string.photos)
        )

        LazyRow(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            item { ItemAddPhoto(connectionState) }
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
fun ItemAddPhoto(connectionState: ConnectionState) {
    val viewModel: NewCatchViewModel = getViewModel()
    val permissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)
    val addPhotoState = rememberSaveable { mutableStateOf(false) }
    val choosePhotoLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { value ->
            value.forEach {
                if (viewModel.images.size < Constants.MAX_PHOTOS) {
                    viewModel.addPhoto(it)
                }

                //TODO: set max photos
            }
        }

    Card(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .padding(end = 4.dp)
            .size(100.dp)
            .fillMaxSize()
            .clip(RoundedCornerShape(5.dp))
            .clickable {
                when (connectionState) {
                    is ConnectionState.Available -> {
                        addPhotoState.value = true
                    }
                    is ConnectionState.Unavailable -> {
                        SnackbarManager.showMessage(R.string.no_internet)
                    }//TODO: no internet }
                }
            },
        elevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colors.primary)
    ) {

        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painterResource(R.drawable.ic_baseline_add_photo_alternate_24), //Or we can use Icons.Default.Add
                contentDescription = Constants.ITEM_ADD_PHOTO,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
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
    choosePhotoLauncher: ManagedActivityResultLauncher<Array<String>, List<Uri>>
) {
    when {
        permissionState.hasPermission -> {
            choosePhotoLauncher.launch(arrayOf("image/*"))
            addPhotoState.value = false
        }
    }
}

@Composable
fun NewCatchWeatherItem(viewModel: NewCatchViewModel, connectionState: ConnectionState) {

    val weather by viewModel.weather.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween.also { Arrangement.Start },
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
            WeatherLayout(weather, viewModel, connectionState, dateAndTime)
        }

        AnimatedVisibility(weatherState is RetrofitWrapper.Loading) {
            if (viewModel.marker.value == null) {
                SecondaryText(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(R.string.select_place_for_weather)
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

@Composable
fun WeatherIconItem(
    iconResource: Int,
    iconTint: Color = Color.Unspecified,
    onIconSelected: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(4.dp)
            .clip(MaterialTheme.shapes.medium)
            .requiredSize(50.dp)
            .clickable(onClick = onIconSelected)
    ) {
        Icon(painterResource(iconResource), "", tint = iconTint)
    }
}


@Composable
fun DateAndTime(
    date: MutableState<Long>,
) {
    val viewModel: NewCatchViewModel = getViewModel()
    val dateSetState = remember { mutableStateOf(false) }
    val timeSetState = remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (dateSetState.value) DatePicker(date, dateSetState, context)
    if (timeSetState.value) TimePicker(date, timeSetState, context)

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
        title = { Text(stringResource(R.string.error_occured)) },
        text = { Text(stringResource(R.string.new_catch_error_description)) },
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


@OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeUiApi::class)
@Composable
fun LoadingDialog() {
    DefaultDialog (primaryText = stringResource(R.string.saving_new_catch),
        content = {
            LoadingAdvertView()
            /*val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.fish_loading))
            LottieAnimation(
                modifier = Modifier.size(128.dp),
                composition = composition,
                iterations = LottieConstants.IterateForever,
                isPlaying = true
            )*/
        },
        negativeButtonText = stringResource(R.string.Cancel),
        onNegativeClick = {},
        onDismiss = {})
}

@Composable
fun LoadingAdvertView(modifier: Modifier = Modifier) {
    val isInEditMode = LocalInspectionMode.current
    if (isInEditMode) {
        Text(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.Red)
                .padding(horizontal = 2.dp, vertical = 6.dp),
            textAlign = TextAlign.Center,
            color = Color.White,
            text = "Advert Here",
        )
    } else {
        AndroidView(
            modifier = modifier.fillMaxWidth().wrapContentHeight(),
            factory = { context ->
                AdView(context).apply {
                    adSize = AdSize.WIDE_SKYSCRAPER
                    adUnitId = context.getString(R.string.new_catch_loading_admob_banner_id)
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
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


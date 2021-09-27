package com.joesemper.fishing.compose.ui.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.joesemper.fishing.R
import com.joesemper.fishing.domain.NewCatchViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.theme.primaryFigmaColor
import com.joesemper.fishing.utils.showToast
import gun0912.tedbottompicker.TedBottomPicker
import kotlinx.coroutines.flow.StateFlow
import org.koin.androidx.compose.getViewModel
import java.util.*


//private val args: NewCatchFragmentArgs by navArgs()

private val dateAndTime = Calendar.getInstance()
private var isNull: Boolean = true

object Constants {
    private const val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 111
    const val ITEM_ADD_PHOTO = "ITEM_ADD_PHOTO"
    const val ITEM_PHOTO = "ITEM_PHOTO"
}

/*
viewModel.marker.value = args.marker as UserMapMarker
isNull = viewModel.marker.value .id.isEmpty()*/


@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalCoilApi
@Composable
fun NewCatchScreen(navController: NavController) {
    val viewModel: NewCatchViewModel = getViewModel()
    Scaffold(
        topBar = { NewCatchAppBar(navController) }
    ) {
        SubscribeToProgress(viewModel.uiState, navController)
        val scrollState = rememberScrollState()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(30.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp)
                .padding(horizontal = 16.dp)
                .verticalScroll(state = scrollState, enabled = true),
        ) {
            Places(stringResource(R.string.place), viewModel)  //Выпадающий список мест

            FishAndWeight(viewModel.fishAmount, viewModel.weight)

            Fishing(viewModel.rod, viewModel.bite, viewModel.lure)
            DateAndTime(viewModel.date, viewModel.time)
            Photos(
                { clicked -> /*TODO(Open photo in full screen)*/ },
                { deleted -> viewModel.deletePhoto(deleted) })
        }
    }
}

@Composable
fun SubscribeToProgress(vmuiState: StateFlow<BaseViewState>, navController: NavController) {
    val errorDialog = rememberSaveable { mutableStateOf(false) }
    val loadingDialog = rememberSaveable { mutableStateOf(false) }
    val loadingValue = rememberSaveable { mutableStateOf(0) }

    val uiState by vmuiState.collectAsState()
    when (uiState) {
        is BaseViewState.Success<*> -> {
            if ((uiState as BaseViewState.Success<*>).data != null) {
                Toast.makeText(
                    LocalContext.current,
                    "Ваш улов успешно добавлен!",
                    Toast.LENGTH_SHORT
                ).show()
                navController.popBackStack()
            }
        }
        is BaseViewState.Loading -> {
            LoadingDialog(loadingDialog, loadingValue)
            loadingValue.value = (uiState as BaseViewState.Loading).progress ?: 0
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
            if (marker.id.isNotEmpty()) marker.title else ""
        )
    }
    var isDropMenuOpen by rememberSaveable { mutableStateOf(false) }
    val suggestions by viewModel.getAllUserMarkersList().collectAsState(listOf())
    val filteredList by rememberSaveable { mutableStateOf(suggestions.toMutableList()) }
    if (textFieldValue == "") searchFor("", suggestions, filteredList)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.align(Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Place,
                stringResource(R.string.place),
                tint = primaryFigmaColor,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.place))
        }
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
                modifier = Modifier.fillMaxWidth().onFocusChanged {
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
                isError = !isThatPlaceInList(textFieldValue, suggestions),
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

//@Composable
//fun Place(label: String) {
//
//    OutlinedTextField(
//        value = viewModel.marker.value.title, onValueChange = { }, readOnly = true,
//        label = { Text(text = label) }, modifier = Modifier.fillMaxWidth()
//    )
//}

/*@ExperimentalMaterialApi
@Composable
private fun BottomSheet() {
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(65.dp)
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.size(20.dp))
        OutlinedButton(
            onClick = { findNavController().popBackStack() }) {
            Text(text = stringResource(R.string.cancel))
        }
        Spacer(modifier = Modifier.size(20.dp))
        OutlinedButton() {
            Text(text = stringResource(R.string.create))
        }
        Spacer(modifier = Modifier.size(20.dp))
    }
}*/

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
        Row(
            modifier = Modifier.align(Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.size(5.dp))
            Icon(
                painterResource(R.drawable.ic_fishing_rod),
                stringResource(R.string.fish_rod),
                modifier = Modifier.size(30.dp),
                tint = primaryFigmaColor
            )
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.fishing_method))
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MyTextField(rod, stringResource(R.string.fish_rod))
            MyTextField(bite, stringResource(R.string.bait))
            MyTextField(lure, stringResource(R.string.lure))
        }

    }
}

@Composable
fun FishAndWeight(fishState: MutableState<String>, weightState: MutableState<String>) {
    val viewModel: NewCatchViewModel = getViewModel()
    Column {

        Row(
            modifier = Modifier.align(Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(R.drawable.ic_fish),
                stringResource(R.string.fish_catch),
                tint = primaryFigmaColor,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.fish_catch))
        }
        FishSpecies(viewModel.title)

        MyTextField(viewModel.description, stringResource(R.string.note))
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
                    ) { Text("-") }
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
                    ) { Text(stringResource(R.string.plus)) }
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
                            if (weightState.value.toDouble() >= 0.5 && weightState.value.isNotBlank())
                                weightState.value =
                                    ((weightState.value.toDouble() - 0.5).toString())
                        },
                        Modifier
                            .weight(1F)
                            .fillMaxHeight()
                            .align(Alignment.CenterVertically)
                    ) { Text(stringResource(R.string.minus)) }
                    Spacer(modifier = Modifier.size(6.dp))
                    OutlinedButton(
                        onClick = {
                            if (weightState.value.isEmpty()) weightState.value =
                                0.5f.toString()
                            else weightState.value =
                                ((weightState.value.toDouble() + 0.5).toString())
                        },
                        Modifier
                            .weight(1F)
                            .fillMaxHeight()
                            .align(Alignment.CenterVertically)
                    ) { Text(stringResource(R.string.plus)) }
                }
            }
        }

    }
}

@ExperimentalAnimationApi
@Composable
fun Photos(
    clickedPhoto: (Uri) -> Unit,
    deletedPhoto: (Uri) -> Unit
) {
    val viewModel: NewCatchViewModel = getViewModel()
    val photos = remember { viewModel.images }
    Column {
        Row(
            modifier = Modifier.align(Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.size(5.dp))
            Icon(
                painterResource(R.drawable.ic_baseline_image_24),
                stringResource(R.string.photos),
                modifier = Modifier.size(30.dp),
                tint = primaryFigmaColor
            )
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.photos))
        }
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

@Composable
fun ItemAddPhoto() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .padding(4.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(5.dp))
                .clickable { addPhoto() },
            elevation = 5.dp,
            backgroundColor = Color.LightGray
        ) {
            Icon(
                painterResource(R.drawable.ic_baseline_add_photo_alternate_24), //Or we can use Icons.Default.Add
                contentDescription = Constants.ITEM_ADD_PHOTO,
                tint = Color.White,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun ItemPhoto(photo: Uri, clickedPhoto: (Uri) -> Unit, deletedPhoto: (Uri) -> Unit) {
    Crossfade(photo) { pic ->
        Box(
            modifier = Modifier
                .size(100.dp)
                .padding(4.dp)
        ) {
            Image(painter = rememberImagePainter(data = pic),
                contentDescription = Constants.ITEM_PHOTO,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(5.dp))
                    .clickable { clickedPhoto(pic) })
            Surface( //For making delete button background half transparent
                color = Color.LightGray.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(25.dp)
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    tint = Color.White,
                    contentDescription = stringResource(R.string.delete_photo),
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { deletedPhoto(pic) })
            }
        }
    }

}

private fun addPhoto() {
    /*val viewModel: NewCatchViewModel = getViewModel()
    if (isPermissionAllowed()) {
        getPhotoListener().showMultiImage { photos ->
            photos.forEach { uri ->
                viewModel.addPhoto(uri)
            }
        }
    } else {
        AlertDialog.Builder(LocalContext.current)
            .setTitle(getString(R.string.permissions_required))
            .setMessage(getString(R.string.add_photo_permission))
            .setPositiveButton(getString(R.string.provide)) { _, _ ->
                askForPermission()
            }
            .setNegativeButton(getString(R.string.deny), null)
            .show()
    }*/
}

@Composable
fun MyTextField(textState: MutableState<String>, label: String) {
    var text by rememberSaveable { textState }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(text = label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next
        ),
        singleLine = true
    )
}

@Composable
fun DateAndTime(dateState: MutableState<String>, timeState: MutableState<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {


        dateState.value = setInitialDate()
        OutlinedTextField(value = dateState.value,
            onValueChange = {},
            label = { Text(text = stringResource(R.string.date)) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    /*showToast(
                        LocalContext.current,
                        stringResource(R.string.click_on_icon_to_change)
                    )*/
                },
            trailingIcon = {
                Icon(painter = painterResource(R.drawable.ic_baseline_event_24),
                    tint = primaryFigmaColor,
                    contentDescription = stringResource(R.string.date),
                    modifier = Modifier.clickable { /*setDate(dateState)*/ })
            })
        timeState.value = setInitialTime()
        OutlinedTextField(value = timeState.value,
            onValueChange = {},
            label = { Text(text = stringResource(R.string.time)) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    /*showToast(
                        LocalContext.current,
                        stringResource(R.string.click_on_icon_to_change)
                    )*/
                },
            trailingIcon = {
                Icon(painter = painterResource(R.drawable.ic_baseline_access_time_24),
                    tint = primaryFigmaColor,
                    contentDescription = stringResource(R.string.time),
                    modifier = Modifier.clickable {
                        /*setTime(timeState)*/
                    })
            })
    }
}

@Composable
fun NewCatchAppBar(navController: NavController) {
    val viewModel: NewCatchViewModel = getViewModel()
    TopAppBar(
        title = { Text(text = stringResource(R.string.new_catch)) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        actions = {
            Row(
                modifier = Modifier.padding(end = 3.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = {
                        if (viewModel.isInputCorrect()) viewModel.createNewUserCatch(
                            getPhotos()
                        )
                        /*else showToast(
                            LocalContext.current,
                            stringResource(R.string.not_all_fields_are_filled)
                        )*/
                    },
                    content = { Icon(Icons.Filled.Done, stringResource(R.string.done)) }
                )
            }
        },
        elevation = 2.dp
    )
}

@Composable
fun ErrorDialog(errorDialog: MutableState<Boolean>) {
    val viewModel: NewCatchViewModel = getViewModel()
    AlertDialog(
        title = { Text("Произошла ошибка!") },
        text = { Text("Не удалось загрузить фотографии. Проверьте интернет соединение и попробуйте еще раз.") },
        onDismissRequest = { errorDialog.value = false },
        confirmButton = {
            OutlinedButton(
                onClick = { viewModel.createNewUserCatch(getPhotos()) },
                content = { Text(stringResource(R.string.Try_again)) })
        }, dismissButton = {
            OutlinedButton(
                onClick = { errorDialog.value = false },
                content = { Text(stringResource(R.string.Cancel)) })
        }
    )
}

@Composable
fun LoadingDialog(loadingDialog: MutableState<Boolean>, loadingValue: MutableState<Int>) {
    AlertDialog(
        title = { Text("Загрузка фотографий!") },
        text = { Text("Пожалуйста, подождите, пока ваши фотографии полностью загрузятся. Текущий прогресс: " + loadingValue.value + "%") },
        onDismissRequest = { },
        confirmButton = {
            OutlinedButton(
                onClick = { },
                content = { Text(stringResource(R.string.Try_again)) })
        }, dismissButton = {
            OutlinedButton(
                onClick = { },
                content = { Text(stringResource(R.string.Cancel)) })
        }
    )
}

private fun getPhotos(): List<ByteArray> {
    /*val viewModel: NewCatchViewModel = getViewModel()
    val result = mutableListOf<ByteArray>()
//    val job = lifecycle.coroutineScope.launchWhenStarted {
//
//    }

    TODO(URI TO BYTEARRAY IN COROUTINE SCOPE)
    viewModel.images.forEach {
//            val baos = ByteArrayOutputStream()
//            val inputStream = requireActivity().contentResolver.openInputStream(it)
//            val bmp = BitmapFactory.decodeStream(inputStream)
//            bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos)
//            result.add(baos.toByteArray())
        requireActivity().contentResolver.openInputStream(it)
            ?.readBytes()
            ?.let { it1 -> result.add(it1) }
    }
    return result*/
    return listOf()
}

@Composable
private fun getPhotoListener() =
    TedBottomPicker.with(LocalContext.current as FragmentActivity?)
        .setPeekHeight(1600)
        .showTitle(false)
        .setCompleteButtonText(stringResource(R.string.done))
        .setEmptySelectionText(stringResource(R.string.no_photo_selected))
        .setSelectMaxCount(10)

@Composable
private fun setTime(timeState: MutableState<String>) {
    TimePickerDialog(
        LocalContext.current,
        TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            dateAndTime.set(Calendar.MINUTE, minute)
            /*timeState.value = setInitialTime()*/
        },
        dateAndTime.get(Calendar.HOUR_OF_DAY),
        dateAndTime.get(Calendar.MINUTE), true
    ).show()
}

@Composable
private fun setInitialTime(): String =
    DateUtils.formatDateTime(
        LocalContext.current,
        dateAndTime.timeInMillis,
        DateUtils.FORMAT_SHOW_TIME
    )

@Composable
private fun setDate(dateState: MutableState<String>) {
    DatePickerDialog(
        LocalContext.current,
        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            dateAndTime.set(Calendar.YEAR, year)
            dateAndTime.set(Calendar.MONTH, monthOfYear)
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            /*dateState.value = setInitialDate()*/
        },
        dateAndTime.get(Calendar.YEAR),
        dateAndTime.get(Calendar.MONTH),
        dateAndTime.get(Calendar.DAY_OF_MONTH)
    ).show()
}

@Composable
private fun setInitialDate() =
    DateUtils.formatDateTime(
        LocalContext.current,
        dateAndTime.timeInMillis,
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
    )

/*private fun askForPermission(): Boolean {
    if (!isPermissionAllowed()) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            showPermissionDeniedDialog()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
            )
        }
        return false
    }
    return true
}

private fun isPermissionAllowed(): Boolean {
    return ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}

override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray,
) {
    when (requestCode) {
        READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE -> {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission is granted, you can perform your operation here
            } else {
                // permission is denied, you can ask for permission again, if you want
                askForPermission()
            }
            return
        }
    }
}

private fun showPermissionDeniedDialog() {
    AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.permission_denied))
        .setMessage(getString(R.string.permission_denied_message))
        .setPositiveButton(getString(R.string.goto_app_settings),
            DialogInterface.OnClickListener { dialogInterface, i ->
                // send to app settings if permission is denied permanently
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            })
        .setNegativeButton(getString(R.string.cancel), null)
        .show()
}

override fun onDetach() {
    super.onDetach()
    (requireActivity() as NavigationHolder).showNav()
}*/

fun commentary() {

    TODO("Subscribe to ViewState")
//    private fun subscribeOnViewModel() {
//        lifecycleScope.launchWhenStarted {
//            viewModel.subscribe().collect { state ->
//                when (state) {
//                    is BaseViewState.Loading -> binding.loading.visibility = View.VISIBLE
//                    is BaseViewState.Success<*> -> binding.loading.visibility = View.GONE
//                    is BaseViewState.Error -> {
//                        binding.loading.visibility = View.GONE
//                        Toast.makeText(context, state.error.message, Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }
//    }

    //TODO("Photo convert from Uri to ByteArray in CoroutineScope")
//        try {
//            val bitmap = MediaStore.Images.Media.getBitmap(c.getContentResolver(), Uri.parse(paths))
//        } catch (e: Exception) {
//            //handle exception
//        }
//        return coroutineScope {
//            val job = launch {
//                try {
//                    uris.forEach {
//                        val stream = requireActivity().contentResolver.openInputStream(it)
//                        val bitmap = BitmapDrawable(resources, stream).bitmap
//                        val baos = ByteArrayOutputStream()
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos)
//                        result.add(baos.toByteArray())
//                    }
//                } catch (e: Throwable) {
//                    Log.d("F", e.message, e)
//                }
//            }
//            job.join()
//            result
//        }


//    private fun setInitialPlaceData() {
//        binding.etNewCatchPlaceTitle.setText(marker.title)
//        binding.etNewCatchPlaceTitle.inputType = InputType.TYPE_NULL
//        setCurrentCoordinates()
//    }
}

//    }
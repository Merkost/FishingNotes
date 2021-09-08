package com.joesemper.fishing.ui.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.google.android.material.transition.MaterialFadeThrough
import com.joesemper.fishing.R
import com.joesemper.fishing.domain.NewCatchViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.RawUserCatch
import com.joesemper.fishing.ui.theme.FigmaTheme
import com.joesemper.fishing.ui.theme.primaryFigmaColor
import com.joesemper.fishing.utils.NavigationHolder
import com.joesemper.fishing.utils.showToast
import gun0912.tedbottompicker.TedBottomPicker
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope
import java.util.*


class NewCatchFragment : Fragment(), AndroidScopeComponent {

    private val args: NewCatchFragmentArgs by navArgs()

    private val dateAndTime = Calendar.getInstance()

    override val scope: Scope by fragmentScope()
    private val viewModel: NewCatchViewModel by viewModel()

    companion object {
        private const val ITEM_ADD_PHOTO = "ITEM_ADD_PHOTO"
        private const val ITEM_PHOTO = "ITEM_PHOTO"
    }

    private lateinit var marker: UserMapMarker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as NavigationHolder).closeNav()
        marker = args.marker as UserMapMarker
    }

    @ExperimentalMaterialApi
    @ExperimentalCoilApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                FigmaTheme {
                    NewCatchScreen()
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @ExperimentalCoilApi
    @Composable
    fun NewCatchScreen() {
        BottomSheetScaffold(
            topBar = { AppBar() },
            sheetContent = { BottomSheet() },
            sheetElevation = 8.dp) {


            val scrollState = rememberScrollState()
            val scaffoldState = rememberBottomSheetScaffoldState()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(state = scrollState, enabled = true),
            ) {
                val name = rememberSaveable { mutableStateOf("") }
                val description = rememberSaveable { mutableStateOf("") }
                val fish = rememberSaveable { mutableStateOf("0") }
                val weight = rememberSaveable { mutableStateOf("0.0") }
                val date = rememberSaveable { mutableStateOf(setInitialDate()) }
                val time = rememberSaveable { mutableStateOf(setInitialTime()) }
                val rod = rememberSaveable { mutableStateOf("") }
                val bite = rememberSaveable { mutableStateOf("") }
                val lure = rememberSaveable { mutableStateOf("") }
                Title(name)
                MyTextField(description, "Описание")
                //Выпадающий список мест
                Place("Место")
                DateAndTime(date, time)
                Spacer(modifier = Modifier.size(4.dp))
                FishAndWeight(fish, weight)

                Spacer(modifier = Modifier.size(4.dp))
                Fishing(rod, bite, lure)

                Photos(
                    { clicked -> /*TODO(Open photo in full screen)*/ },
                    { deleted -> viewModel.deletePhoto(deleted) })

//                SaveButton(
//                    name.value, description.value, fish.value, weight.value, date.value,
//                    time.value, rod.value, bite.value, lure.value
//                )
            }

        }
    }

    @ExperimentalMaterialApi
    @Composable
    private fun BottomSheet() {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(55.dp).fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.size(20.dp))
            OutlinedButton(
                onClick = { findNavController().popBackStack() }) {
                Text(text = "Cancel")
            }
            Spacer(modifier = Modifier.size(20.dp))
            OutlinedButton(onClick = {
//                if (isInputCorrect(name)) {
//                    /*val catch = createNewUserCatch(
//                        name, description, fish, weight, date,
//                        time, rod, bite, lure
//                    )
//                    viewModel.addNewCatch(catch)
////                    when(viewModel.subscribe().collectAsState()) { TODO
////
////                    }
//                    findNavController().popBackStack()*/
//                }
            }) {
                Text(text = "Create")
            }
            Spacer(modifier = Modifier.size(20.dp))
        }
    }

    @Composable
    fun Title(nameState: MutableState<String>) {
        Spacer(modifier = Modifier.size(4.dp))
        Column {
            OutlinedTextField(
                value = nameState.value,
                onValueChange = { nameState.value = it },
                label = { Text("Заголовок") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameState.value.isBlank()
            )
            Spacer(modifier = Modifier.size(2.dp))
            Text(text = " *Required", fontSize = 12.sp)
        }
    }

    @Composable
    fun SaveButton(
//        name: String,
//        description: String,
//        fish: String,
//        weight: String,
//        date: String,
//        time: String,
//        rod: String,
//        bite: String,
//        lure: String
    ) {

    }

    @Composable
    fun Place(label: String) {
        OutlinedTextField(
            value = marker.title, onValueChange = { }, readOnly = true,
            label = { Text(text = label) }, modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    fun Fishing(rod: MutableState<String>, bite: MutableState<String>, lure: MutableState<String>) {
        Column {
            Row(
                modifier = Modifier.align(Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.size(5.dp))
                Icon(
                    painterResource(R.drawable.ic_fishing_rod), "Удочка",
                    modifier = Modifier.size(30.dp), tint = primaryFigmaColor
                )
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.fishing_method))
            }
            MyTextField(rod, "Удочка")
            MyTextField(bite, "Блесна")
            MyTextField(lure, "Наживка")
        }
    }

    @Composable
    fun FishAndWeight(fishState: MutableState<String>, weightState: MutableState<String>) {
        Column {
            Row(
                modifier = Modifier.align(Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.ic_fish),
                    "Рыба",
                    tint = primaryFigmaColor,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.fish_catch))
            }
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
                        label = { Text(text = "Кол-во рыбы") },
                        trailingIcon = { Text("PC.") },
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
                            Modifier.weight(1F).fillMaxHeight().align(Alignment.CenterVertically)
                        ) { Text("-") }
                        Spacer(modifier = Modifier.size(6.dp))
                        OutlinedButton(
                            onClick = {
                                if (fishState.value.isEmpty()) fishState.value = 1.toString()
                                else fishState.value = ((fishState.value.toInt() + 1).toString())
                            },
                            Modifier.weight(1F).fillMaxHeight().align(Alignment.CenterVertically)
                        ) { Text("+") }
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
                        label = { Text(text = "Вес") },
                        trailingIcon = {
                            Text("KG.")
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
                            Modifier.weight(1F).fillMaxHeight().align(Alignment.CenterVertically)
                        ) { Text("_") }
                        Spacer(modifier = Modifier.size(6.dp))
                        OutlinedButton(
                            onClick = {
                                if (weightState.value.isEmpty()) weightState.value = 0.5f.toString()
                                else weightState.value =
                                    ((weightState.value.toDouble() + 0.5).toString())
                            },
                            Modifier.weight(1F).fillMaxHeight().align(Alignment.CenterVertically)
                        ) { Text("+") }
                    }
                }
            }
        }
    }

    @Composable
    fun Photos(
        clickedPhoto: (String) -> Unit,
        deletedPhoto: (String) -> Unit
    ) {
        val photos = remember { viewModel.images }
        Column {
            Row(
                modifier = Modifier.align(Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.size(5.dp))
                Icon(
                    painterResource(R.drawable.ic_baseline_image_24), "Фото",
                    modifier = Modifier.size(30.dp), tint = primaryFigmaColor
                )
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.photos))
            }
            LazyRow(/*cells = GridCells.Fixed(2), */modifier = Modifier.fillMaxSize()) {
                item { ItemAddPhoto() }
                items(
                    items = photos,
                    itemContent = {
                        ItemPhoto(
                            photo = it,
                            clickedPhoto = clickedPhoto,
                            deletedPhoto = deletedPhoto
                        )
                    })
            }
        }
        Spacer(modifier = Modifier.size(60.dp))
    }

    @Composable
    fun ItemAddPhoto() {
        Box(
            modifier = Modifier.size(100.dp).padding(4.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxSize().clickable { addPhoto() }, elevation = 5.dp,
                backgroundColor = Color.LightGray
            ) {
                Icon(
                    painterResource(R.drawable.ic_baseline_add_photo_alternate_24), //Or we can use Icons.Default.Add
                    contentDescription = ITEM_ADD_PHOTO,
                    tint = Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                )
            }
        }
    }

    @Composable
    fun ItemPhoto(photo: String, clickedPhoto: (String) -> Unit, deletedPhoto: (String) -> Unit) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .padding(4.dp)
        ) {
            Image(painter = rememberImagePainter(data = photo),
                contentDescription = "ITEM_PHOTO",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().clickable { clickedPhoto(photo) })
            Surface( //For making delete button background half transparent
                color = Color.LightGray.copy(alpha = 0.2f),
                modifier = Modifier.size(25.dp).align(Alignment.TopEnd).padding(3.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    tint = Color.White,
                    contentDescription = "DELETE",
                    modifier = Modifier
                        .fillMaxSize().clickable { deletedPhoto(photo) })
            }
        }
    }

    private fun addPhoto() {
        getPhotoListener().showMultiImage { photos ->
            photos.forEach { uri ->
                viewModel.addPhoto(uri.toString())
            }
        }
    }

//    @Composable
//    private fun Place() {
//        OutlinedTextField(
//            value = marker.title,
//            onValueChange = { }, //text -> if (text !== marker.title) onValueChange(text) },
//            modifier = Modifier.fillMaxWidth(),
//            label = marker.title
//        )
//        DropdownMenu(
//            expanded = true, //suggestions.isNotEmpty(),
//            onDismissRequest = {  },
//            modifier = Modifier.fillMaxWidth(),
//            // This line here will accomplish what you want
//            properties = PopupProperties(focusable = false)
//        ) {
////            suggestions.forEach { label ->
////                DropdownMenuItem(onClick = {
////                    onOptionSelected(label)
////                }) {
////                    Text(text = label)
////                }
////            }
//        }
//    }

    @Composable
    fun MyTextField(textState: MutableState<String>, label: String) {
        OutlinedTextField(
            value = textState.value,
            onValueChange = { textState.value = it },
            label = { Text(text = label) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            )
        )
    }

    @Composable
    fun DateAndTime(dateState: MutableState<String>, timeState: MutableState<String>) {
        OutlinedTextField(value = dateState.value, onValueChange = {},
            label = { Text(text = "Дата") }, readOnly = true, modifier = Modifier
                .fillMaxWidth()
                .clickable { showToast(requireContext(), "Click on the icon to change!") },
            trailingIcon = {
                Icon(painter = painterResource(R.drawable.ic_baseline_event_24),
                    tint = primaryFigmaColor,
                    contentDescription = "Дата",
                    modifier = Modifier.clickable { setDate(dateState) })
            })
        OutlinedTextField(value = timeState.value, onValueChange = {},
            label = { Text(text = "Время") }, readOnly = true, modifier = Modifier
                .fillMaxWidth()
                .clickable { showToast(requireContext(), "Click on the icon to change!") },
            trailingIcon = {
                Icon(painter = painterResource(R.drawable.ic_baseline_access_time_24),
                    tint = primaryFigmaColor,
                    contentDescription = "Время",
                    modifier = Modifier.clickable {
                        setTime(timeState)
                    })
            })
    }

    @Composable
    fun AppBar() {
        TopAppBar(
            title = { Text(text = "New catch") },
            navigationIcon = {
                IconButton(onClick = { findNavController().popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = getString(R.string.back)
                    )
                }
            },
            elevation = 2.dp
        )
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
////        setTransactions()
//        subscribeOnViewModel()
//        setInitialData()
//        initViews()
//        setOnClickListeners()
//        initRV()
//
//    }

    private fun setTransactions() {
        enterTransition = MaterialFadeThrough()
        returnTransition = MaterialFadeThrough()
    }

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

//    private fun initRV() {
//        adapter = AddNewPhotosAdapter { item ->
//            when (item) {
//                is PhotosRecyclerViewItem.ItemAddNewPhoto -> {
//                    getPhotoListener()
//                        .showMultiImage { photos ->
//                            photos.forEach { uri ->
//                                uris.add(uri)
//                                adapter.addItem(uri.toString())
//                            }
//                        }
//                }
//                is PhotosRecyclerViewItem.ItemPhoto -> {
//                    adapter.deleteItem(item)
//                }
//            }
//
//        }
//        binding.rvPhotos.layoutManager =
//            GridLayoutManager(context, 3)
//        binding.rvPhotos.adapter = adapter
//    }

//    private fun initToolbar() {
//        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbarNewCatch)
//    }

//    private fun initBottomDialog() {
//        (requireActivity() as NavigationHolder).closeNav()
//        val bottomSheetBehavior = BottomSheetBehavior.from(
//            requireActivity().findViewById(R.id.bottomSheet)
//        )
//        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//    }

//    private fun setInitialPlaceData() {
//        binding.etNewCatchPlaceTitle.setText(marker.title)
//        binding.etNewCatchPlaceTitle.inputType = InputType.TYPE_NULL
//        setCurrentCoordinates()
//    }

    private fun getPhotoListener() =
        TedBottomPicker.with(requireActivity())
            .setPeekHeight(1600)
            .showTitle(false)
            .setCompleteButtonText("Done")
            .setEmptySelectionText("No Select")
            .setSelectMaxCount(10)

//    private fun setCurrentCoordinates() {
//        binding.etNewCatchCoordinates.inputType = InputType.TYPE_NULL
//
//        val latitude = marker.latitude.format(3)
//        val longitude = marker.longitude.format(3)
//
//        "Lat: $latitude  Lon: $longitude"
//            .also { binding.etNewCatchCoordinates.setText(it) }
//    }


    private fun setTime(timeState: MutableState<String>) {
        TimePickerDialog(
            requireContext(),
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                dateAndTime.set(Calendar.MINUTE, minute)
                timeState.value = setInitialTime()
            },
            dateAndTime.get(Calendar.HOUR_OF_DAY),
            dateAndTime.get(Calendar.MINUTE), true
        ).show()
    }

    private fun setInitialTime(): String =
        DateUtils.formatDateTime(
            requireContext(),
            dateAndTime.timeInMillis,
            DateUtils.FORMAT_SHOW_TIME
        )

    private fun setDate(dateState: MutableState<String>) {
        DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                dateAndTime.set(Calendar.YEAR, year)
                dateAndTime.set(Calendar.MONTH, monthOfYear)
                dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                dateState.value = setInitialDate()
            },
            dateAndTime.get(Calendar.YEAR),
            dateAndTime.get(Calendar.MONTH),
            dateAndTime.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setInitialDate() =
        DateUtils.formatDateTime(
            requireContext(),
            dateAndTime.timeInMillis,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
        )


    private fun isInputCorrect(title: String): Boolean {
        return title.isNotBlank()
    }

    private fun createNewUserCatch(
        title: String, description: String, fish: String,
        weight: String, date: String, time: String,
        rod: String, bite: String, lure: String
    ) = RawUserCatch(
        title = title,
        description = description,
        time = time,
        date = date,
        fishType = fish,
//        fishAmount = fish.toInt(),
        fishWeight = weight.toDouble(),
        fishingRodType = rod,
        fishingBait = bite,
        fishingLure = lure,
        markerId = marker.id,
        isPublic = false,
        photos = getPhotos()
    )


    private fun getPhotos(): List<ByteArray> {

        val result = mutableListOf<ByteArray>()
        val job = lifecycle.coroutineScope.launchWhenStarted {

        }

        /*TODO(URI TO BYTEARRAY IN COROUTINE SCOPE)*/
        viewModel.images.forEach {
            requireActivity().contentResolver.openInputStream(Uri.parse(it))
                ?.readBytes()
                ?.let { it1 -> result.add(it1) }
        }

        return result

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
    }

    override fun onDetach() {
        super.onDetach()
        (requireActivity() as NavigationHolder).showNav()
    }
}
package com.joesemper.fishing.ui.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
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
import com.joesemper.fishing.utils.NavigationHolder
import com.joesemper.fishing.utils.showToast
import gun0912.tedbottompicker.TedBottomPicker
import kotlinx.coroutines.InternalCoroutinesApi
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
        marker = args.marker as UserMapMarker
    }

    @ExperimentalFoundationApi
    @InternalCoroutinesApi
    @ExperimentalCoilApi
    @ExperimentalMaterialApi
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

    @ExperimentalFoundationApi
    @InternalCoroutinesApi
    @ExperimentalMaterialApi
    @ExperimentalCoilApi
    @Composable
    fun NewCatchScreen() {
        Scaffold(
            topBar = { AppBar() }) {
            val scrollState = rememberScrollState()
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
                val fish = rememberSaveable { mutableStateOf("") }
                val weight = rememberSaveable { mutableStateOf("0") }
                val date = rememberSaveable { mutableStateOf(setInitialDate()) }
                val time = rememberSaveable { mutableStateOf(setInitialTime()) }
                val rod = rememberSaveable { mutableStateOf("") }
                val bite = rememberSaveable { mutableStateOf("") }
                val lure = rememberSaveable { mutableStateOf("") }
                Spacer(modifier = Modifier.size(4.dp))
                Column {
                    MyRequiredTextField(name, "Заголовок", name.value.isBlank())
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(text = " *Required", fontSize = 12.sp)
                }
                MyTextField(description, "Описание")
                //Выпадающий список мест
                Place("Место")
                MyTextField(fish, "Рыба")
                Weight(weight)
                Date(date)
                Time(time)
                MyTextField(rod, "Удочка")
                MyTextField(bite, "Блесна")
                MyTextField(lure, "Наживка")
                Text(text = "Фото", modifier = Modifier.align(Alignment.Start))
                Photos({ clicked ->
                    if (clicked == ITEM_ADD_PHOTO) {
                        getPhotoListener().showMultiImage { photos ->
                            photos.forEach { uri ->
                                viewModel.addPhoto(uri.toString())
                            }
                        }
                    }
                }, { deleted ->
                    viewModel.deletePhoto(deleted)
                })
                //Spacer(modifier = Modifier.size(8.dp))
                SaveButton(
                    name.value, description.value, fish.value, weight.value, date.value,
                    time.value, rod.value, bite.value, lure.value
                )
            }

        }
    }

    @Composable
    fun SaveButton(
        name: String,
        description: String,
        fish: String,
        weight: String,
        date: String,
        time: String,
        rod: String,
        bite: String,
        lure: String
    ) {
        Row(horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = { findNavController().popBackStack() }) {
                Text(text = "Cancel")
            }
            Spacer(modifier = Modifier.size(10.dp))
            OutlinedButton(onClick = {
                if (isInputCorrect(name)) {
                    val catch = createNewUserCatch(
                        name, description, fish, weight, date,
                        time, rod, bite, lure
                    )
                    viewModel.addNewCatch(catch)
//                    when(viewModel.subscribe().collectAsState()) { TODO
//
//                    }
                    findNavController().popBackStack()
                }
            }) {
                Text(text = "Create")
            }
        }
        Spacer(modifier = Modifier.size(6.dp))
    }

    @Composable
    fun Place(label: String) {
        OutlinedTextField(
            value = marker.title, onValueChange = { }, readOnly = true,
            label = { Text(text = label) }, modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    fun Weight(weightState: MutableState<String>) {
        Column {
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
                label = { Text(text = "Вес (кг)") },
                trailingIcon = {
                    if (weightState.value.isNotBlank()) IconButton(
                        content = { Icon(Icons.Default.Clear, contentDescription = "Minus") },
                        onClick = { weightState.value = "" }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
//            Spacer(modifier = Modifier.size(6.dp))
//            Row(Modifier.fillMaxWidth()) {
//                OutlinedButton(
//                    onClick = { if (weightState.value.toDouble() > 0) weightState.value.toD -= 0.5 },
//                    Modifier
//                        .weight(1F)
//                        .fillMaxHeight()
//                        .align(Alignment.CenterVertically)
//                ) {
//                    Icon(Icons.Default.Clear, contentDescription = "Minus")
//                }
//                Spacer(modifier = Modifier.size(6.dp))
//                OutlinedButton(
//                    onClick = { weightState.value += 0.5 },
//                    Modifier
//                        .weight(1F)
//                        .fillMaxHeight()
//                        .align(Alignment.CenterVertically)
//                ) {
//                    Icon(Icons.Default.Add, contentDescription = "Plus")
//                }
//            }
        }
    }

    @ExperimentalFoundationApi
    @Composable
    fun Photos(clickedPhoto: (String) -> Unit, deletedPhoto: (String) -> Unit) {
        val photos = remember { viewModel.images }
        LazyRow(/*cells = GridCells.Fixed(2), */modifier = Modifier.fillMaxSize()) {
            items(
                items = photos,
                itemContent = {
                    PhotoItem(photo = it, selectedPhoto = clickedPhoto, deletedPhoto = deletedPhoto)
                })
        }
    }

    @Composable
    fun PhotoItem(photo: String, selectedPhoto: (String) -> Unit, deletedPhoto: (String) -> Unit) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .padding(4.dp)
        ) {
            if (photo == ITEM_ADD_PHOTO)
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { selectedPhoto(photo) }, elevation = 5.dp,
                    backgroundColor = Color.LightGray
                ) {
                    Icon(
                        Icons.Default.Add, contentDescription = ITEM_ADD_PHOTO, tint = Color.White,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    )
                }
            else {
                Image(
                    painter = rememberImagePainter(
                        data = photo/*,
                    builder = {
                        transformations(CircleCropTransformation())
                    }*/
                    ),
                    contentDescription = ITEM_PHOTO,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
                Icon(
                    Icons.Default.Close,
                    tint = Color.White,
                    contentDescription = "DELETE",
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.TopEnd)
                        .padding(3.dp)
                        .clip(CircleShape)
                        .clickable { deletedPhoto(photo) }
                )
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
    fun MyRequiredTextField(textState: MutableState<String>, label: String, isError: Boolean) {
        OutlinedTextField(
            value = textState.value,
            onValueChange = { textState.value = it },
            label = { Text(text = label) },
            modifier = Modifier.fillMaxWidth(),
            isError = isError
        )
    }

    @Composable
    fun Time(timeState: MutableState<String>) {
        OutlinedTextField(value = timeState.value, onValueChange = {},
            label = { Text(text = "Время") }, readOnly = true, modifier = Modifier
                .fillMaxWidth()
                .clickable { showToast(requireContext(), "Click on the icon to change!") },
            trailingIcon = {
                Image(painter = painterResource(R.drawable.ic_baseline_access_time_24),
                    contentDescription = "Время", modifier = Modifier.clickable {
                        setTime(timeState)
                    })
            })
    }

    @Composable
    fun Date(dateState: MutableState<String>) {
        OutlinedTextField(value = dateState.value, onValueChange = {},
            label = { Text(text = "Дата") }, readOnly = true, modifier = Modifier
                .fillMaxWidth()
                .clickable { showToast(requireContext(), "Click on the icon to change!") },
            trailingIcon = {
                Image(painter = painterResource(R.drawable.ic_baseline_event_24),
                    contentDescription = "Дата", modifier = Modifier.clickable {
                        setDate(dateState)
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
//
//    private fun setOnCreateClickListener() {
//        val buttonCreate = requireActivity().findViewById<Button>(R.id.button_new_catch_create)
//        buttonCreate.setOnClickListener {
//            val catch = createNewUserCatch()
//            viewModel.addNewCatch(catch)
//            findNavController().popBackStack()
//        }
//    }

//    private fun setOnCloseClickListeners() {
//        with(binding) {
//            toolbarNewCatch.setNavigationOnClickListener {
//                findNavController().popBackStack()
//            }
//        }
//        val buttonCancel = requireActivity().findViewById<Button>(R.id.button_new_catch_cancel)
//        buttonCancel.setOnClickListener {
//            findNavController().popBackStack()
//        }
//    }

//    private fun setOnEditTimeAndDateClickListeners() {
//        with(binding) {
//            textInputLayoutNewCatchDate.setEndIconOnClickListener { setDate() }
//            textInputLayoutNewCatchTime.setEndIconOnClickListener { setTime() }
//            etNewCatchDate.inputType = InputType.TYPE_NULL
//            etNewCatchTime.inputType = InputType.TYPE_NULL
//        }
//
//    }

//    private fun setCurrentCoordinates() {
//        binding.etNewCatchCoordinates.inputType = InputType.TYPE_NULL
//
//        val latitude = marker.latitude.format(3)
//        val longitude = marker.longitude.format(3)
//
//        "Lat: $latitude  Lon: $longitude"
//            .also { binding.etNewCatchCoordinates.setText(it) }
//    }
//
//    private fun setInputListeners() {
//        binding.etNewCatchTitle.addTextChangedListener(
//
//        )
//        binding.etNewCatchKindOfFish.addTextChangedListener {
//
//        }
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


    private fun isInputCorrect(title: String, fish: String): Boolean {
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
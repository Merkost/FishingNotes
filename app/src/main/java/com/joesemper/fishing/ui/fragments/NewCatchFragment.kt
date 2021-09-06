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
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.transition.MaterialFadeThrough
import com.joesemper.fishing.R
import com.joesemper.fishing.domain.NewCatchViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.adapters.AddNewPhotosAdapter
import com.joesemper.fishing.ui.theme.FigmaTheme
import com.joesemper.fishing.utils.NavigationHolder
import com.joesemper.fishing.utils.showToast
import gun0912.tedbottompicker.TedBottomPicker
import kotlinx.coroutines.*
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
    }

    private lateinit var marker: UserMapMarker

    private lateinit var adapter: AddNewPhotosAdapter

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
            topBar = { AppBar() },
            content = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(ScrollState(0), enabled = true),
                ) {
                    Spacer(modifier = Modifier.size(16.dp))
                    TextField("Заголовок")
                    Spacer(modifier = Modifier.size(8.dp))
                    TextField("Описание")
                    Spacer(modifier = Modifier.size(8.dp))
                    //Выпадающий список мест
                    //Place()
                    Spacer(modifier = Modifier.size(8.dp))
                    TextField("Рыба")
                    Spacer(modifier = Modifier.size(8.dp))
                    TextField("Вес")
                    Spacer(modifier = Modifier.size(8.dp))
                    Date()
                    Spacer(modifier = Modifier.size(8.dp))
                    Time()
                    Spacer(modifier = Modifier.size(8.dp))
                    TextField("Удочка")
                    Spacer(modifier = Modifier.size(8.dp))
                    TextField("Блесна")
                    Spacer(modifier = Modifier.size(8.dp))
                    TextField("Наживка")
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "Фото", modifier = Modifier.align(Alignment.Start))
                    Photos() {
                        getPhotoListener().showMultiImage { photos ->
                            photos.forEach { uri ->
                                viewModel.addPhoto(uri.toString())
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))


                }
            })
    }

    @ExperimentalFoundationApi
    @Composable
    fun Photos(clickedPhoto: (Int)->Unit) {
        val photos = remember { viewModel.images }
        LazyRow(/*cells = GridCells.Fixed(2), */modifier = Modifier.fillMaxSize()) {
            items(
                items = photos,
                itemContent = {
                    PhotoItem(photo = it, selectedPhoto = clickedPhoto)
                })
        }

    }

    @Composable
    fun PhotoItem(photo: String, selectedPhoto: (Int) -> Unit) {
        Card(modifier = Modifier
            .size(150.dp)
            .clickable { selectedPhoto(photo.length) }, elevation = 8.dp) {
            if(photo == ITEM_ADD_PHOTO)
                Image(painter = painterResource(R.drawable.ic_baseline_plus),
                    contentDescription = ITEM_ADD_PHOTO,
                    modifier = Modifier.size(10.dp))
            else
                Image(painter = rememberImagePainter(
                    data = photo,
                    builder = {
                        transformations(CircleCropTransformation())
                    }),
                    contentDescription = ITEM_ADD_PHOTO,
                    modifier = Modifier.size(10.dp))
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
    fun TextField(label: String) {
        var value by remember { mutableStateOf("") }
        OutlinedTextField(
            value = value, onValueChange = { value = it },
            label = { Text(text = label) }, modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    fun Time() {
        val timeState = remember { mutableStateOf(setInitialTime()) }
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
    fun Date() {
        val dateState = remember { mutableStateOf(setInitialDate()) }
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

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
//    ): View {
//        binding = FragmentNewCatchBinding.inflate(layoutInflater, container, false)
//        return binding.root
//    }

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

//    private fun initViews() {
//        initToolbar()
//        initBottomDialog()
//    }
//
//    private fun setInitialData() {
//        setInitialTimeAndDate()
//        setInitialPlaceData()
//    }
//
//    private fun setOnClickListeners() {
//        setOnIncrementDecrementClickListeners()
//        setOnCreateClickListener()
//        setOnCloseClickListeners()
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

    private fun initBottomDialog() {
        (requireActivity() as NavigationHolder).closeNav()
        val bottomSheetBehavior = BottomSheetBehavior.from(
            requireActivity().findViewById(R.id.bottomSheet)
        )
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

//    private fun setInitialPlaceData() {
//        binding.etNewCatchPlaceTitle.setText(marker.title)
//        binding.etNewCatchPlaceTitle.inputType = InputType.TYPE_NULL
//        setCurrentCoordinates()
//    }

//    private fun setInitialTimeAndDate() {
//        setInitialDate()
//        setInitialTime()
//        setOnEditTimeAndDateClickListeners()
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


//    private fun isInputCorrect(): Boolean {
//        var isCorrect = true
//        if (binding.etNewCatchTitle.text.isNullOrBlank()) {
//            binding.textInputLayoutNewCatchTitle.error = "Enter title"
//            isCorrect = false
//        }
//        if (binding.etNewCatchKindOfFish.text.isNullOrBlank()) {
//            binding.textInputLayoutNewCatchKindOfFish.error = "Enter kind of fish"
//            isCorrect = false
//        }
//        return isCorrect
//    }

//    private fun createNewUserCatch() = RawUserCatch(
//        title = binding.etNewCatchTitle.text.toString(),
//        description = binding.etNewCatchDescription.text.toString(),
//        time = binding.etNewCatchTime.text.toString(),
//        date = binding.etNewCatchDate.text.toString(),
//        fishType = binding.etNewCatchKindOfFish.text.toString(),
//        fishAmount = binding.etNewCatchAmount.text.toString().toInt(),
//        fishWeight = binding.etNewCatchAmount.text.toString().toDouble(),
//        fishingRodType = binding.etNewCatchRod.text.toString(),
//        fishingBait = binding.etNewCatchBait.text.toString(),
//        fishingLure = binding.etNewCatchLure.text.toString(),
//        markerId = marker.id,
//        isPublic = binding.switchPublishCatch.isChecked,
//        photos = getPhotos()
//    )


    private fun getPhotos(): List<ByteArray> {

        val result = mutableListOf<ByteArray>()
        return result
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
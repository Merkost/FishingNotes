package com.joesemper.fishing.fragments

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.transition.MaterialFadeThrough
import com.joesemper.fishing.R
import com.joesemper.fishing.adapters.AddNewPhotosAdapter
import com.joesemper.fishing.adapters.PhotosRecyclerViewItem
import com.joesemper.fishing.data.entity.content.UserMapMarker
import com.joesemper.fishing.data.entity.raw.RawUserCatch
import com.joesemper.fishing.databinding.FragmentNewCatchBinding
import com.joesemper.fishing.utils.NavigationHolder
import com.joesemper.fishing.utils.format
import com.joesemper.fishing.utils.roundTo
import com.joesemper.fishing.viewmodels.NewCatchViewModel
import com.joesemper.fishing.viewmodels.viewstates.NewCatchViewState
import gun0912.tedbottompicker.TedBottomPicker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope
import java.util.*


class NewCatchFragment : Fragment(), AndroidScopeComponent {

    private val args: com.joesemper.fishing.fragments.NewCatchFragmentArgs by navArgs()

    private val dateAndTime = Calendar.getInstance()

    override val scope: Scope by fragmentScope()
    private val viewModel: NewCatchViewModel by viewModel()

    private lateinit var binding: FragmentNewCatchBinding

    private lateinit var marker: UserMapMarker

    private lateinit var adapter: AddNewPhotosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        marker = args.marker as UserMapMarker
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View { binding = FragmentNewCatchBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        setTransactions()
        subscribeOnViewModel()
        setInitialData()
        initViews()
        setOnClickListeners()
        initRV()

    }

    private fun setTransactions() {
        enterTransition = MaterialFadeThrough()
        returnTransition = MaterialFadeThrough()
    }

    private fun subscribeOnViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.subscribe().collect { state ->
                when (state) {
                    is NewCatchViewState.Loading -> binding.loading.visibility = View.VISIBLE
                    is NewCatchViewState.Success -> binding.loading.visibility = View.GONE
                    is NewCatchViewState.Error -> {
                        binding.loading.visibility = View.GONE
                        Toast.makeText(context, state.error.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun initViews() {
        initToolbar()
        initBottomDialog()
    }

    private fun setInitialData() {
        setInitialTimeAndDate()
        setInitialPlaceData()
    }

    private fun setOnClickListeners() {
        setOnIncrementDecrementClickListeners()
        setOnCreateClickListener()
        setOnCloseClickListeners()
    }

    private val uris = mutableListOf<Uri>()

    private fun initRV() {
        adapter = AddNewPhotosAdapter { item ->
            when (item) {
                is PhotosRecyclerViewItem.ItemAddNewPhoto -> {
                    getPhotoListener()
                        .showMultiImage { photos ->
                            photos.forEach { uri ->
                                uris.add(uri)
                                adapter.addItem(uri.toString())
                            }
                        }
                }
                is PhotosRecyclerViewItem.ItemPhoto -> {
                    adapter.deleteItem(item)
                }
            }

        }
        binding.rvPhotos.layoutManager =
            GridLayoutManager(context, 3)
        binding.rvPhotos.adapter = adapter
    }

    private fun initToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbarNewCatch)
    }

    private fun initBottomDialog() {
        (requireActivity() as NavigationHolder).closeNav()
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setInitialPlaceData() {
        binding.etNewCatchPlaceTitle.setText(marker.title)
        binding.etNewCatchPlaceTitle.inputType = InputType.TYPE_NULL
        setCurrentCoordinates()
    }

    private fun setInitialTimeAndDate() {
        setInitialDate()
        setInitialTime()
        setOnEditTimeAndDateClickListeners()
    }

    private fun getPhotoListener() =
        TedBottomPicker.with(requireActivity())
            .setPeekHeight(1600)
            .showTitle(false)
            .setCompleteButtonText("Done")
            .setEmptySelectionText("No Select")
            .setSelectMaxCount(10)


    private fun setOnIncrementDecrementClickListeners() {
        with(binding) {
            buttonPlusAmount.setOnClickListener {
                etNewCatchAmount.setText(
                    (etNewCatchAmount.text.toString().toInt().plus(1)).toString()
                )
            }
            buttonMinusAmount.setOnClickListener {
                if (etNewCatchAmount.text.toString().toInt() <= 0) return@setOnClickListener
                etNewCatchAmount.setText(
                    (etNewCatchAmount.text.toString().toInt().minus(1)).toString()
                )
            }
            buttonPlusWeight.setOnClickListener {
                etNewCatchWeight.setText(
                    (etNewCatchWeight.text.toString().toDouble().plus(0.1).roundTo(1)).toString()
                )
            }
            buttonMinusWeight.setOnClickListener {
                if (etNewCatchWeight.text.toString().toDouble() <= 0) return@setOnClickListener
                etNewCatchWeight.setText(
                    (etNewCatchWeight.text.toString().toDouble().minus(0.1).roundTo(1)).toString()
                )
            }
        }
    }

    private fun setOnCreateClickListener() {
        val buttonCreate = binding.bottomSheet.findViewById<Button>(R.id.button_new_catch_create)
        buttonCreate.setOnClickListener {
            val catch = createNewUserCatch()
            viewModel.addNewCatch(catch)
            findNavController().popBackStack()
        }
    }

    private fun setOnCloseClickListeners() {
        with(binding) {
            toolbarNewCatch.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
        val buttonCancel = binding.bottomSheet.findViewById<Button>(R.id.button_new_catch_cancel)
        buttonCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setOnEditTimeAndDateClickListeners() {
        with(binding) {
            textInputLayoutNewCatchDate.setEndIconOnClickListener { setDate() }
            textInputLayoutNewCatchTime.setEndIconOnClickListener { setTime() }
            etNewCatchDate.inputType = InputType.TYPE_NULL
            etNewCatchTime.inputType = InputType.TYPE_NULL
        }

    }

    private fun setCurrentCoordinates() {
        binding.etNewCatchCoordinates.inputType = InputType.TYPE_NULL

        val latitude = marker.latitude.format(3)
        val longitude = marker.longitude.format(3)

        "Lat: $latitude  Lon: $longitude"
            .also { binding.etNewCatchCoordinates.setText(it) }
    }

    private fun setInputListeners() {
        binding.etNewCatchTitle.addTextChangedListener(

        )
        binding.etNewCatchKindOfFish.addTextChangedListener {

        }
    }

    private fun setDate() {
        DatePickerDialog(
            requireContext(),
            dateSetListener,
            dateAndTime.get(Calendar.YEAR),
            dateAndTime.get(Calendar.MONTH),
            dateAndTime.get(Calendar.DAY_OF_MONTH)
        )
            .show()
    }

    private fun setTime() {
        TimePickerDialog(
            requireContext(),
            timeSetListener,
            dateAndTime.get(Calendar.HOUR_OF_DAY),
            dateAndTime.get(Calendar.MINUTE), true
        )
            .show()
    }

    private fun setInitialDate() {
        binding.etNewCatchDate.setText(
            DateUtils.formatDateTime(
                requireContext(),
                dateAndTime.timeInMillis,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
            )
        )
    }

    private fun setInitialTime() {
        binding.etNewCatchTime.setText(
            DateUtils.formatDateTime(
                requireContext(),
                dateAndTime.timeInMillis,
                DateUtils.FORMAT_SHOW_TIME
            )
        )
    }

    private val timeSetListener =
        OnTimeSetListener { view, hourOfDay, minute ->
            dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            dateAndTime.set(Calendar.MINUTE, minute)
            setInitialTime()
        }

    private val dateSetListener =
        OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            dateAndTime.set(Calendar.YEAR, year)
            dateAndTime.set(Calendar.MONTH, monthOfYear)
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            setInitialDate()
        }

    private fun isInputCorrect(): Boolean {
        var isCorrect = true
        if (binding.etNewCatchTitle.text.isNullOrBlank()) {
            binding.textInputLayoutNewCatchTitle.error = "Enter title"
            isCorrect = false
        }
        if (binding.etNewCatchKindOfFish.text.isNullOrBlank()) {
            binding.textInputLayoutNewCatchKindOfFish.error = "Enter kind of fish"
            isCorrect = false
        }
        return isCorrect
    }

    private fun createNewUserCatch() = RawUserCatch(
        title = binding.etNewCatchTitle.text.toString(),
        description = binding.etNewCatchDescription.text.toString(),
        time = binding.etNewCatchTime.text.toString(),
        date = binding.etNewCatchDate.text.toString(),
        fishType = binding.etNewCatchKindOfFish.text.toString(),
        fishAmount = binding.etNewCatchAmount.text.toString().toInt(),
        fishWeight = binding.etNewCatchAmount.text.toString().toDouble(),
        fishingRodType = binding.etNewCatchRod.text.toString(),
        fishingBait = binding.etNewCatchBait.text.toString(),
        fishingLure = binding.etNewCatchLure.text.toString(),
        markerId = marker.id,
        isPublic = binding.switchPublishCatch.isChecked,
        photos = getPhotos()
    )


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
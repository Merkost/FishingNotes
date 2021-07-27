package com.joesemper.fishing.presentation.map.catches

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.joesemper.fishing.R
import com.joesemper.fishing.data.entity.RawUserCatch
import com.joesemper.fishing.databinding.FragmentNewCatchBinding
import com.joesemper.fishing.model.common.content.UserMapMarker
import com.joesemper.fishing.utils.NavigationHolder
import com.joesemper.fishing.utils.format
import com.joesemper.fishing.utils.roundTo
import gun0912.tedbottompicker.TedBottomPicker
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_new_catch.*
import kotlinx.android.synthetic.main.fragment_new_catch.*
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope
import java.io.ByteArrayOutputStream
import java.util.*

class NewCatchFragment : Fragment(), AndroidScopeComponent {

    private val args: NewCatchFragmentArgs by navArgs()

    private val dateAndTime = Calendar.getInstance()

    override val scope: Scope by fragmentScope()
    private val viewModel: NewCatchViewModel by viewModel()

    private var _binding: FragmentNewCatchBinding? = null
    private val binding
        get() = _binding!!

    private var currentPhotos = mutableListOf<Uri>()

    private lateinit var marker: UserMapMarker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewCatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setInitialData()
        initViews()
        setOnClickListeners()
    }

    private fun initViews() {
        initToolbar()
        initBottomDialog()
    }

    private fun setInitialData() {
        marker = args.marker as UserMapMarker
        setInitialPlaceData()
        setInitialTimeAndDate()
    }

    private fun setOnClickListeners() {
        setOnAddPhotoClickListener()
        setOnIncrementDecrementClickListeners()
        setOnCreateClickListener()
        setOnCloseClickListeners()
    }

    private fun initToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbarNewCatch)
    }

    private fun initBottomDialog() {
        (requireActivity() as NavigationHolder).closeNav()
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
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

    private fun setOnAddPhotoClickListener() {
        binding.buttonNewCatchAddImages.setOnClickListener {
            TedBottomPicker.with(requireActivity())
                .setPeekHeight(1600)
                .showTitle(false)
                .setCompleteButtonText("Done")
                .setEmptySelectionText("No Select")
                .setSelectMaxCount(3)
                .showMultiImage { uriList ->
                    currentPhotos = uriList
                    if (uriList.isNotEmpty()) {
                        for (i in 0..uriList.size) {
                            when (i) {
                                0 -> binding.ivNewCatchImageFirst.load(uriList[i])
                                1 -> binding.ivNewCatchImageSecond.load(uriList[i])
                                2 -> binding.ivNewCatchImageThird.load(uriList[i])
                            }
                        }
                    }
                }
        }
    }

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
        val buttonCreate = bottomSheet.findViewById<Button>(R.id.button_new_catch_create)
        buttonCreate.setOnClickListener {
            viewModel.addNewCatch(createNewUserCatch())
            findNavController().popBackStack()
        }
    }

    private fun setOnCloseClickListeners() {
        with(binding) {
            toolbarNewCatch.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
        val buttonCancel = bottomSheet.findViewById<Button>(R.id.button_new_catch_cancel)
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
        if (currentPhotos.isEmpty()) {
            return listOf()
        }
        val bitmaps = mutableListOf<ByteArray>()
        for (i in 0..currentPhotos.size) {
            when (i) {
                0 -> bitmaps.add(getByteArrayFromImageVew(binding.ivNewCatchImageFirst))
                1 -> bitmaps.add(getByteArrayFromImageVew(binding.ivNewCatchImageSecond))
                2 -> bitmaps.add(getByteArrayFromImageVew(binding.ivNewCatchImageThird))

            }
        }
        return bitmaps
    }

    private fun getByteArrayFromImageVew(view: ImageView): ByteArray {
        val bitmap = (view.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }

    override fun onDetach() {
        super.onDetach()
        (requireActivity() as NavigationHolder).showNav()
    }
}
package com.joesemper.fishing.view.fragments.dialogFragments

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import coil.load
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.joesemper.fishing.R
import com.joesemper.fishing.data.entity.RawUserCatch
import com.joesemper.fishing.databinding.FragmentNewCatchBinding
import com.joesemper.fishing.model.common.content.MapMarker
import com.joesemper.fishing.utils.format
import com.joesemper.fishing.utils.getCurrentUser
import com.joesemper.fishing.utils.getNewMarkerId
import gun0912.tedbottompicker.TedBottomPicker
import java.io.ByteArrayOutputStream
import java.util.*


class AddMarkerBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        private const val LAT_LNG = "LAT_LNG"

        fun newInstance(latLng: LatLng): BottomSheetDialogFragment {
            val args = bundleOf(LAT_LNG to latLng)
            val fragment = AddMarkerBottomSheetDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val dateAndTime = Calendar.getInstance()

    private var _binding: FragmentNewCatchBinding? = null
    private val binding get() = _binding!!

    private var currentPhotos = mutableListOf<Uri>()

    private var currentCoordinates: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewCatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setCurrentCoordinates()
        setOnApplyClickListener()
        setOnCloseClickListeners()
        setOnAddPhotoClickListener()
        setOnSelectPlaceClickListener()
        initTimeAndDate()
    }

    private fun setCurrentCoordinates() {
        val coordinates = arguments?.getParcelable<LatLng>(LAT_LNG)
        if (coordinates != null) {
            currentCoordinates = coordinates
            val latitude = coordinates.latitude.format(3)
            val longitude = coordinates.longitude.format(3)

            "Latitude: $latitude \nLongitude: $longitude"
                .also { binding.tvNewCatchLatitudeLongitude.text = it }
        }
    }

    private fun setOnApplyClickListener() {
        binding.buttonNewCatchCreate.setOnClickListener {
            if (isInputCorrect()) {
                val catch = createNewUserCatch()
                (parentFragment as AddNewCatchListener).addNewCatch(catch)
                dismiss()
            }
        }
    }

    private fun initTimeAndDate() {
        setInitialDate()
        setInitialTime()
        setOnEditTimeAndDateClickListeners()
    }

    private fun setOnEditTimeAndDateClickListeners() {
        with(binding) {
            tvNewCatchDate.setOnClickListener { setDate() }
            tvNewCatchTime.setOnClickListener { setTime() }
            buttonNewCatchEditDate.setOnClickListener { setDate() }
            buttonNewCatchEditTime.setOnClickListener { setTime() }
        }

    }

    private fun setInputListeners() {
        binding.etNewCatchTitle.addTextChangedListener(

        )
        binding.etNewCatchKindOfFish.addTextChangedListener {

        }
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

                    for(i in 0..uriList.size) {
                        when (i) {
                            0 -> binding.ivNewCatchImageFirst.load(uriList[i])
                            1 -> binding.ivNewCatchImageSecond.load(uriList[i])
                            2 -> binding.ivNewCatchImageThird.load(uriList[i])

                        }
                    }

                }
        }
    }

    private fun setOnSelectPlaceClickListener() {
        binding.buttonNewCatchPlaceSelect.setOnClickListener { view ->
            showSelectPlaceMenu(view)
        }
    }

    private fun setOnCloseClickListeners() {
        with(binding) {
            toolbarNewCatch.setNavigationOnClickListener { dismiss() }
            buttonNewCatchCancel.setOnClickListener { dismiss() }
        }
    }

    // отображаем диалоговое окно для выбора даты
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

    // отображаем диалоговое окно для выбора времени
    private fun setTime() {
        TimePickerDialog(
            requireContext(),
            timeSetListener,
            dateAndTime.get(Calendar.HOUR_OF_DAY),
            dateAndTime.get(Calendar.MINUTE), true
        )
            .show()
    }

    // установка начальных даты и времени
    private fun setInitialDate() {
        binding.tvNewCatchDate.text = DateUtils.formatDateTime(
            requireContext(),
            dateAndTime.timeInMillis,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
        )
    }

    private fun setInitialTime() {
        binding.tvNewCatchTime.text = DateUtils.formatDateTime(
            requireContext(),
            dateAndTime.timeInMillis,
            DateUtils.FORMAT_SHOW_TIME
        )
    }

    // установка обработчика выбора времени
    private val timeSetListener =
        OnTimeSetListener { view, hourOfDay, minute ->
            dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            dateAndTime.set(Calendar.MINUTE, minute)
            setInitialTime()
        }

    // установка обработчика выбора даты
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

    private fun showSelectPlaceMenu(v: View) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(R.menu.menu_new_catch_lication_select, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.item_current -> {
                    Toast.makeText(requireContext(), "Current", Toast.LENGTH_SHORT).show()
                }
                R.id.item_on_map -> {
                    Toast.makeText(requireContext(), "On Map", Toast.LENGTH_SHORT).show()
                }
                R.id.item_markers_list -> {
                    Toast.makeText(requireContext(), "Markers list", Toast.LENGTH_SHORT).show()
                }
            }
           true
        }
        popup.show()
    }

    private fun createNewUserCatch() =  RawUserCatch(
            title = binding.etNewCatchTitle.text.toString(),
            description = binding.etNewCatchDescription.text.toString(),
            time = binding.tvNewCatchTime.text.toString(),
            date = binding.tvNewCatchDate.text.toString(),
            fishType = binding.etNewCatchKindOfFish.text.toString(),
            fishAmount = binding.etNewCatchAmount.text.toString().toInt(),
            fishWeight = binding.etNewCatchAmount.text.toString().toDouble(),
            fishingRodType = binding.etNewCatchRod.text.toString(),
            fishingBait = binding.etNewCatchBait.text.toString(),
            fishingLure = binding.etNewCatchLure.text.toString(),
            marker = createUserMarker(),
            isPublic = binding.switchPublishCatch.isChecked,
            photos = getPhotos()
        )

    private fun createUserMarker(): MapMarker {
        val coordinates = currentCoordinates
        return if (coordinates != null) {
            MapMarker(
                id = getNewMarkerId(),
                latitude = coordinates.latitude,
                longitude = coordinates.longitude,
                title = binding.etNewCatchPlaceTitle.text.toString()
            )
        } else {
            MapMarker()
        }

    }
    private fun getPhotos():  List<ByteArray> {
        if (currentPhotos.isEmpty()) {
            return listOf()
        }
        val bitmaps = mutableListOf<ByteArray>()
        for(i in 0..currentPhotos.size) {
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

}

interface AddNewCatchListener {
    fun addNewCatch(newCatch: RawUserCatch)
}
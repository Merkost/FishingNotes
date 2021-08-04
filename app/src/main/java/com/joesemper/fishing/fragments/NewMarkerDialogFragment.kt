package com.joesemper.fishing.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.joesemper.fishing.data.entity.raw.RawMapMarker
import com.joesemper.fishing.databinding.FragmentNewMarkerBinding
import com.joesemper.fishing.utils.AddNewMarkerListener
import com.joesemper.fishing.utils.format

class NewMarkerDialogFragment : BottomSheetDialogFragment() {

    companion object {
        private const val PLACE = "PLACE"

        fun newInstance(latLng: LatLng): DialogFragment {
            val args = bundleOf(PLACE to latLng)
            val fragment = NewMarkerDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: FragmentNewMarkerBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var coordinates: LatLng

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewMarkerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setCurrentCoordinates()
        setOnCloseClickListeners()
        setOnCreateClickListener()
    }

    private fun setCurrentCoordinates() {
        binding.etCoordinates.inputType = InputType.TYPE_NULL
        val latLng = arguments?.getParcelable<LatLng>(PLACE)
        if (latLng != null) {
            coordinates = latLng
            val latitude = coordinates.latitude.format(3)
            val longitude = coordinates.longitude.format(3)

            "Lat: $latitude  Lon: $longitude".also { binding.etCoordinates.setText(it) }
        }
    }

    private fun setOnCreateClickListener() {
        binding.buttonCreate.setOnClickListener {
            val marker = createRawMarker()
            try {
                (parentFragment as AddNewMarkerListener).addNewMapMarker(marker)
            } catch (e: Throwable) {
                Toast.makeText(context, "Can not create marker", Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }
    }


    private fun setOnCloseClickListeners() {
        with(binding) {
            toolbarNewMarker.setNavigationOnClickListener { dismiss() }
            buttonCancel.setOnClickListener { dismiss() }
        }
    }

    private fun createRawMarker(): RawMapMarker {
        return RawMapMarker(
            title = binding.etTitle.text.toString(),
            description = binding.etDescription.text.toString(),
            latitude = coordinates.latitude,
            longitude = coordinates.longitude
        )
    }
}


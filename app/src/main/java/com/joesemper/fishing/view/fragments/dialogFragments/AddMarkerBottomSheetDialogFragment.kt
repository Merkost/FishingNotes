package com.joesemper.fishing.view.fragments.dialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.map.UserMarker
import com.joesemper.fishing.utils.createUserMarker
import kotlinx.android.synthetic.main.fragment_bottom_sheet_dialog_add_marker.*

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet_dialog_add_marker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnApplyClickListener()
        setOnCloseClickListener()
    }

    private fun setOnApplyClickListener() {
        button_add_marker_apply.setOnClickListener {
            val title = et_add_marker_title.text.toString()
            val description = et_add_marker_description.text.toString()
            val latLng = arguments?.getParcelable<LatLng>(LAT_LNG)
            val marker = createUserMarker(latLng!!, title, description)
            (parentFragment as AddMarkerListener).addMarker(marker)
            dismiss()
        }

    }

    private fun setOnCloseClickListener() {
        button_add_marker_close.setOnClickListener {
            dismiss()
        }
    }
}

interface AddMarkerListener {
    fun addMarker(marker: UserMarker)
}
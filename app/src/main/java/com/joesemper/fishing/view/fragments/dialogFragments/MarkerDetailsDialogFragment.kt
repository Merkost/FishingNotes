package com.joesemper.fishing.view.fragments.dialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.map.UserMarker
import kotlinx.android.synthetic.main.fragmet_dialog_marker_details.*

interface DeleteMarkerListener {
    fun deleteMarker(marker: UserMarker)
}

class MarkerDetailsDialogFragment : DialogFragment() {

    companion object {
        private const val MARKER = "MARKER"

        fun newInstance(userMarker: UserMarker): DialogFragment {
            val args = bundleOf(MARKER to userMarker)
            val fragment = MarkerDetailsDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragmet_dialog_marker_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userMarker = arguments?.getParcelable<UserMarker>(MARKER)

        if (userMarker != null) {
            setHeader(userMarker.title)
            setDescription(userMarker.description)
            setOnDeleteButtonClickListener(userMarker)
        }
    }

    private fun setHeader(header: String) {
        tv_marker_deatails_header.text = header
    }

    private fun setDescription(description: String?) {
        tv_marker_details_description.text = description
    }

    private fun setOnDeleteButtonClickListener(userMarker: UserMarker) {
        button_delete_marker.setOnClickListener {
            (parentFragment as DeleteMarkerListener).deleteMarker(userMarker)
            dismiss()
        }
    }

}
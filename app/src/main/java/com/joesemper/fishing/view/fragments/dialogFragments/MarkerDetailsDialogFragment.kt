package com.joesemper.fishing.view.fragments.dialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import coil.load
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.common.UserCatch
import kotlinx.android.synthetic.main.fragmet_dialog_marker_details.*

interface DeleteMarkerListener {
    fun deleteMarker(aCatch: UserCatch)
}

class MarkerDetailsDialogFragment : DialogFragment() {

    companion object {
        private const val MARKER = "MARKER"

        fun newInstance(userCatch: UserCatch): DialogFragment {
            val args = bundleOf(MARKER to userCatch)
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

        val userMarker = arguments?.getParcelable<UserCatch>(MARKER)

        if (userMarker != null) {
            setHeader(userMarker.title)
            setDescription(userMarker.description)
//            setImage(userMarker.downloadPhotoLink)
            setOnDeleteButtonClickListener(userMarker)
        }
    }

    private fun setHeader(header: String) {
        tv_marker_deatails_header.text = header
    }

    private fun setDescription(description: String?) {
        tv_marker_details_description.text = description
    }

    private fun setImage(img: String?) {
        if (!img.isNullOrBlank()) {
            iv_marker_details_photo.load(img.toUri())
        }
    }

    private fun setOnDeleteButtonClickListener(userCatch: UserCatch) {
        button_delete_marker.setOnClickListener {
            (parentFragment as DeleteMarkerListener).deleteMarker(userCatch)
            dismiss()
        }
    }

}
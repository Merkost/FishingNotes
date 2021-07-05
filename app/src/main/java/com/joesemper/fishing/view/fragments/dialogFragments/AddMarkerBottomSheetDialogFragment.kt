package com.joesemper.fishing.view.fragments.dialogFragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import coil.load
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.map.UserMarker
import com.joesemper.fishing.utils.createUserMarker
import com.joesemper.fishing.utils.getTimeStamp
import kotlinx.android.synthetic.main.fragment_bottom_sheet_dialog_add_marker.*
import java.io.File
import java.io.IOException

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

    lateinit var currentPhotoPath: String
    private var currentPhotoUri: Uri? = null

    private val registeredActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(result)
        }

    private val registeredGalleryPhotoActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onGalleryPhotoResult(result)
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
        setOnAddPhotoClickListener()
    }

    private fun setOnApplyClickListener() {
        button_add_marker_apply.setOnClickListener {
            val title = et_add_marker_title.text.toString()
            val description = et_add_marker_description.text.toString()
            val latLng = arguments?.getParcelable<LatLng>(LAT_LNG)
            val marker = createUserMarker(latLng!!, title, description, currentPhotoUri.toString())
            (parentFragment as AddMarkerListener).addMarker(marker)
            dismiss()
        }

    }

    private fun setOnAddPhotoClickListener() {
        button_add_photo.setOnClickListener {
            dispatchTakePictureIntent()
        }
        button_add_photo_from_gallery.setOnClickListener {
            val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            registeredGalleryPhotoActivity.launch(pickPhoto)
        }
    }

    private fun onActivityResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            iv_marker_photo.load(currentPhotoUri)
        }
    }

    private fun onGalleryPhotoResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImage: Uri? = result.data?.data
            if (selectedImage != null) {
                iv_marker_photo.load(selectedImage)
                currentPhotoUri = selectedImage
            }
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir: File? =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${getTimeStamp()}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                    null
                }
                photoFile?.also { file ->
                    currentPhotoUri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.joesemper.fishing.fileprovider",
                        file
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
                    registeredActivity.launch(takePictureIntent)
                }
            }
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
package com.joesemper.fishing.presentation.map.dialogs.create.catches.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.load
import com.joesemper.fishing.databinding.ItemAddPhotoBinding
import com.joesemper.fishing.databinding.ItemPhotoBinding

class AddNewPhotosAdapter(
    private val onItemClicked: (PhotosRecyclerViewItem) -> Unit
) : RecyclerView.Adapter<AddNewPhotosAdapter.PhotosViewHolder>() {

    companion object {
        private const val ITEM_ADD_PHOTO = 0
        private const val ITEM_PHOTO = 1
    }

    var data = mutableListOf<PhotosRecyclerViewItem>(PhotosRecyclerViewItem.ItemAddNewPhoto)

    fun addItem(uri: String) {
        val item = PhotosRecyclerViewItem.ItemPhoto(uri)
        data.add(item)
        notifyDataSetChanged()
    }

    fun deleteItem(item: PhotosRecyclerViewItem.ItemPhoto) {
        data.remove(item)
        notifyDataSetChanged()
    }

    sealed class PhotosViewHolder(private val binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        class PhotoViewHolder(private val binding: ItemPhotoBinding) :
            PhotosViewHolder(binding) {

            fun bind(item: PhotosRecyclerViewItem) {
                val photo = (item as PhotosRecyclerViewItem.ItemPhoto)
                binding.ivPhoto.load(photo.photoUri)
            }
        }

        class AddPhotoViewHolder(private val binding: ItemAddPhotoBinding) :
            PhotosViewHolder(binding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotosViewHolder {
        return when (viewType) {
            ITEM_PHOTO-> PhotosViewHolder.PhotoViewHolder(
                ItemPhotoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            ITEM_ADD_PHOTO -> PhotosViewHolder.AddPhotoViewHolder(
                ItemAddPhotoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }

    }

    override fun onBindViewHolder(holder: PhotosViewHolder, position: Int) {
        when (holder) {
            is PhotosViewHolder.PhotoViewHolder -> {
                val item = data[position]
                holder.bind(item)
                holder.itemView.setOnClickListener {
                    onItemClicked(item)
                }
            }
            is PhotosViewHolder.AddPhotoViewHolder -> {
                val item = data[position]
                holder.itemView.setOnClickListener {
                    onItemClicked(item)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> ITEM_ADD_PHOTO
            else -> ITEM_PHOTO
        }
    }

    override fun getItemCount() = data.size

}

sealed class PhotosRecyclerViewItem {

    object ItemAddNewPhoto : PhotosRecyclerViewItem()

    class ItemPhoto(
        val photoUri: String
    ) : PhotosRecyclerViewItem()

}

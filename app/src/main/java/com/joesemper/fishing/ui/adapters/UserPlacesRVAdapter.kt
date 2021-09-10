package com.joesemper.fishing.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.joesemper.fishing.databinding.ItemAddNewPlaceBinding
import com.joesemper.fishing.databinding.ItemUserPlaceBinding
import com.joesemper.fishing.model.entity.content.UserMapMarker

class UserPlacesRVAdapter(
    private val data: List<UserMapMarker>,
    private val onItemClicked: (PlaceRecyclerViewItem) -> Unit
) : RecyclerView.Adapter<UserPlacesRVAdapter.PlacesViewHolder>() {

    companion object {
        private const val ITEM_ADD_NEW_PLACE = 0
        private const val ITEM_USER_PLACE = 1
    }

    sealed class PlacesViewHolder(binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        class PlaceViewHolder(private val binding: ItemUserPlaceBinding) :
            PlacesViewHolder(binding) {

            fun bind(item: PlaceRecyclerViewItem) {
                val place = (item as PlaceRecyclerViewItem.ItemUserPlace).place
                with(binding) {

                    tvPlaceTitle.text = place.title

                    if (!place.description.isNullOrEmpty()) {
                        tvPlaceDescription.text = place.description
                    }
                }
            }
        }

        class AddNewCatchViewHolder(private val binding: ItemAddNewPlaceBinding) :
            PlacesViewHolder(binding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesViewHolder {
        return when (viewType) {
            ITEM_USER_PLACE -> PlacesViewHolder.PlaceViewHolder(
                ItemUserPlaceBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            ITEM_ADD_NEW_PLACE-> PlacesViewHolder.AddNewCatchViewHolder(
                ItemAddNewPlaceBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {
        when (holder) {
            is PlacesViewHolder.PlaceViewHolder -> {
                val item = getItem(position)
                holder.bind(item)
                holder.itemView.setOnClickListener {
                    onItemClicked(item)
                }
            }
            is PlacesViewHolder.AddNewCatchViewHolder -> {
                val item = getItem(position)
                holder.itemView.setOnClickListener {
                    onItemClicked(item)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> ITEM_ADD_NEW_PLACE
            else -> ITEM_USER_PLACE
        }
    }

    override fun getItemCount() = data.size + 1

    private fun getItem(position: Int): PlaceRecyclerViewItem {
        return when (getItemViewType(position)) {
            ITEM_ADD_NEW_PLACE -> PlaceRecyclerViewItem.ItemAddNewPlace
            ITEM_USER_PLACE -> PlaceRecyclerViewItem.ItemUserPlace(data[position - 1])
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}

sealed class PlaceRecyclerViewItem {

    object ItemAddNewPlace : PlaceRecyclerViewItem()

    class ItemUserPlace(
        val place: UserMapMarker
    ) : PlaceRecyclerViewItem()

}


package com.joesemper.fishing.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.joesemper.fishing.databinding.ItemAddNewPlaceBinding
import com.joesemper.fishing.databinding.ItemAddNewUserCatchBinding
import com.joesemper.fishing.databinding.ItemUserPlaceBinding
import com.joesemper.fishing.model.entity.content.UserMapMarker

class UserPlacesRVAdapter(
    private val onItemClicked: (PlaceRecyclerViewItem) -> Unit
) : RecyclerView.Adapter<UserPlacesRVAdapter.PlacesViewHolder>() {

    companion object {
        private const val ITEM_ADD_NEW_PLACE = 0
        private const val ITEM_USER_PLACE = 1
    }

    val data = mutableListOf<PlaceRecyclerViewItem>(PlaceRecyclerViewItem.ItemAddNewPlace)

    fun addData(catches: List<UserMapMarker>) {
        data.clear()
        data.add(PlaceRecyclerViewItem.ItemAddNewPlace)
        catches.forEach{ userPlace ->
            data.add(PlaceRecyclerViewItem.ItemUserPlace(userPlace))
        }
        notifyDataSetChanged()
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
                val item = data[position]
                holder.bind(item)
                holder.itemView.setOnClickListener {
                    onItemClicked(item)
                }
            }
            is PlacesViewHolder.AddNewCatchViewHolder -> {
                val item = data[position]
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

    override fun getItemCount() = data.size
}

sealed class PlaceRecyclerViewItem {

    object ItemAddNewPlace : PlaceRecyclerViewItem()

    class ItemUserPlace(
        val place: UserMapMarker
    ) : PlaceRecyclerViewItem()

}


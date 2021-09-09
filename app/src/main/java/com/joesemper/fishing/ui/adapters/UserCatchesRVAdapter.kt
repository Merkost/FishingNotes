package com.joesemper.fishing.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.load
import com.joesemper.fishing.databinding.ItemAddNewUserCatchBinding
import com.joesemper.fishing.databinding.ItemUserCatchBinding
import com.joesemper.fishing.model.entity.content.UserCatch

class UserCatchesRVAdapter(
    private val data: List<UserCatch>,
    private val onItemClicked: (CatchRecyclerViewItem) -> Unit
) : RecyclerView.Adapter<UserCatchesRVAdapter.CatchesViewHolder>() {

    companion object {
        private const val ITEM_ADD_NEW_CATCH = 0
        private const val ITEM_USER_CATCH = 1
    }

    sealed class CatchesViewHolder(binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        class CatchViewHolder(private val binding: ItemUserCatchBinding) :
            CatchesViewHolder(binding) {

            fun bind(item: CatchRecyclerViewItem) {
                val catch = (item as CatchRecyclerViewItem.ItemUserCatch).catch
                with(binding) {

                    tvCatchItemFish.text = catch.fishType

                    "Amount: ${catch.fishAmount}".also { tvCatchItemAmount.text = it }

                    "${catch.fishWeight} KG".also { tvCatchItemWeight.text = it }

                    if (catch.downloadPhotoLinks.isNotEmpty()) {
                        ivCatchItemPhoto.load(catch.downloadPhotoLinks.first())
                    }

                    "${catch.date} ${catch.time}".also { tvCatchItemTimeDate.text = it }
                }


            }
        }

        class AddNewCatchViewHolder(binding: ItemAddNewUserCatchBinding) :
            CatchesViewHolder(binding)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatchesViewHolder {
        return when (viewType) {
            ITEM_USER_CATCH -> CatchesViewHolder.CatchViewHolder(
                ItemUserCatchBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            ITEM_ADD_NEW_CATCH -> CatchesViewHolder.AddNewCatchViewHolder(
                ItemAddNewUserCatchBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }

    }

    override fun onBindViewHolder(holder: CatchesViewHolder, position: Int) {
        when (holder) {
            is CatchesViewHolder.CatchViewHolder -> {
                val item = getItem(position)
                holder.bind(item)
                holder.itemView.setOnClickListener {
                    onItemClicked(item)
                }
            }
            is CatchesViewHolder.AddNewCatchViewHolder -> {
                val item = getItem(position)
                holder.itemView.setOnClickListener {
                    onItemClicked(item)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> ITEM_ADD_NEW_CATCH
            else -> ITEM_USER_CATCH
        }
    }

    override fun getItemCount() = data.size + 1

    private fun getItem(position: Int): CatchRecyclerViewItem {
        return when (getItemViewType(position)) {
            ITEM_ADD_NEW_CATCH -> CatchRecyclerViewItem.ItemAddNewCatch
            ITEM_USER_CATCH -> CatchRecyclerViewItem.ItemUserCatch(data[position - 1])
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

}

sealed class CatchRecyclerViewItem {

    object ItemAddNewCatch : CatchRecyclerViewItem()

    class ItemUserCatch(
        val catch: UserCatch
    ) : CatchRecyclerViewItem()

}
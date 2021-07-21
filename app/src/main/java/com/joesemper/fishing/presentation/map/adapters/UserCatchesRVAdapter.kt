package com.joesemper.fishing.presentation.map.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.joesemper.fishing.R
import com.joesemper.fishing.model.common.content.UserCatch
import kotlinx.android.extensions.LayoutContainer

class UserCatchesRVAdapter(
    private val data: List<UserCatch>
): RecyclerView.Adapter<UserCatchesRVAdapter.CatchesViewHolder>() {


    class CatchesViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        private val tvFish = containerView.findViewById<TextView>(R.id.tv_catch_item_fish)
        private val tvAmount = containerView.findViewById<TextView>(R.id.tv_catch_item_amount)
        private val tvWeight = containerView.findViewById<TextView>(R.id.tv_catch_item_weight)
        private val ivPhoto = containerView.findViewById<ImageView>(R.id.iv_catch_item_photo)

        fun bind(catch: UserCatch) {
            tvFish.text = catch.fishType
            tvAmount.text = catch.fishAmount.toString()
            tvWeight.text = catch.fishWeight.toString()
            if (catch.downloadPhotoLinks.isNotEmpty()) {
                ivPhoto.load(catch.downloadPhotoLinks.first())
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatchesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_catch, parent, false)
        return CatchesViewHolder(view)
    }

    override fun onBindViewHolder(holder: CatchesViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

}
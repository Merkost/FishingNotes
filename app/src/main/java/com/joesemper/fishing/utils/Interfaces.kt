package com.joesemper.fishing.utils

import com.joesemper.fishing.data.entity.RawMapMarker
import com.joesemper.fishing.model.common.content.UserCatch


interface OnCatchListItemClickListener {
    fun onItemClick(catch: UserCatch)
}

interface AddNewMarkerListener {
    fun addNewMapMarker(marker: RawMapMarker)
}

interface NavigationHolder {
    fun closeNav()
    fun showNav()
}

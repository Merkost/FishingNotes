package com.joesemper.fishing.utils

import com.joesemper.fishing.data.entity.raw.RawMapMarker

interface AddNewMarkerListener {
    fun addNewMapMarker(marker: RawMapMarker)
}

interface NavigationHolder {
    fun closeNav()
    fun showNav()
}

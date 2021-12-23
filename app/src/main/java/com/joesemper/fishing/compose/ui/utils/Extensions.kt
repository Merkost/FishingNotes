package com.joesemper.fishing.compose.ui.utils

import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker

fun List<UserMapMarker>.myPlacesSort(placesSortValue: String): List<UserMapMarker> {
    return when (placesSortValue) {
        PlacesSortValues.TimeAsc.name -> { sortedBy { it.dateOfCreation } }
        PlacesSortValues.TimeDesc.name -> { sortedByDescending { it.dateOfCreation } }
        PlacesSortValues.NameAsc.name -> { sortedBy { it.title } }
        PlacesSortValues.NameDesc.name -> { sortedByDescending { it.dateOfCreation } }
        else -> this
    }
}


fun List<UserCatch>.myCatchesSort(catchesSortValue: String): List<UserCatch> {
    return when (catchesSortValue) {
        CatchesSortValues.TimeAsc.name -> { sortedBy { it.date } }
        CatchesSortValues.TimeDesc.name -> { sortedByDescending { it.date } }
        CatchesSortValues.NameAsc.name -> { sortedBy { it.fishType } }
        CatchesSortValues.NameDesc.name -> { sortedByDescending { it.fishType } }
        else -> this
    }
}
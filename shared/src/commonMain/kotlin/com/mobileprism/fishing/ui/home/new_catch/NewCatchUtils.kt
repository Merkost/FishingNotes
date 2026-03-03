package com.mobileprism.fishing.ui.home.new_catch

import com.mobileprism.fishing.domain.entity.content.UserMapMarker


sealed class ReceivedPlaceState() {
    object NotReceived : ReceivedPlaceState()
    class Received(val place: UserMapMarker) : ReceivedPlaceState()
}

sealed class NewCatchPlacesState() {
    object NotReceived : NewCatchPlacesState()
    class Received(val locations: List<UserMapMarker>) : NewCatchPlacesState()
}

sealed class DropdownMenuState {
    object Closed : DropdownMenuState()
    object Opened : DropdownMenuState()
}

fun isThatPlaceInList(
    textFieldValue: String,
    suggestions: List<UserMapMarker>
): Boolean {
    suggestions.forEach {
        if (it.title == textFieldValue) return true
    }
    return false
}

fun searchFor(
    what: String,
    where: List<UserMapMarker>,
    filteredList: MutableList<UserMapMarker>
) {
    filteredList.clear()
    where.forEach {
        if (it.title.contains(what, ignoreCase = true)) {
            filteredList.add(it)
        }
    }
}
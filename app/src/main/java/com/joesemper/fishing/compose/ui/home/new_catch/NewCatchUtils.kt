package com.joesemper.fishing.compose.ui.home.new_catch

import com.joesemper.fishing.model.entity.content.UserMapMarker

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
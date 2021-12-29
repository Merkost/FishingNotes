package com.joesemper.fishing.compose.ui.utils

import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker

enum class PlacesSortValues(val stringRes: Int) {
    Default (R.string.default_word),
    TimeAsc (R.string.time_asc),
    TimeDesc (R.string.time_desc),
    NameAsc (R.string.name_asc),
    NameDesc (R.string.name_desc);

    fun sort(list: List<UserMapMarker>): List<UserMapMarker> {
        return when (this) {
            Default -> list
            TimeAsc -> { list.sortedBy { it.dateOfCreation } }
            TimeDesc -> { list.sortedByDescending { it.dateOfCreation } }
            NameAsc -> { list.sortedBy { it.title } }
            NameDesc -> { list.sortedByDescending { it.dateOfCreation } }
        }
    }
}

enum class CatchesSortValues(val stringRes: Int) {
    Default (R.string.default_word),
    TimeAsc (R.string.time_asc),
    TimeDesc (R.string.time_desc),
    NameAsc (R.string.name_asc),
    NameDesc (R.string.name_desc);

    fun sort(list: List<UserCatch>): List<UserCatch> {
        return when (this) {
            Default -> list
            TimeAsc -> { list.sortedBy { it.date } }
            TimeDesc -> { list.sortedByDescending { it.date } }
            NameAsc -> { list.sortedBy { it.fishType } }
            NameDesc -> { list.sortedByDescending { it.fishType } }
        }
    }
}
package com.mobileprism.fishing.ui.utils.enums

import com.mobileprism.fishing.R
import com.mobileprism.fishing.model.entity.content.UserCatch
import com.mobileprism.fishing.model.entity.content.UserMapMarker

enum class PlacesSortValues(override val stringRes: Int) : StringOperation {
    Default (R.string.default_word),
    TimeAsc (R.string.time_asc),
    TimeDesc (R.string.time_desc),
    NameAsc (R.string.name_asc),
    NameDesc (R.string.name_desc),
    CatchesDesc (R.string.catches_desc);

    fun sort(list: List<UserMapMarker>): List<UserMapMarker> {
        return when (this) {
            Default -> list
            TimeAsc -> { list.sortedBy { it.dateOfCreation } }
            TimeDesc -> { list.sortedByDescending { it.dateOfCreation } }
            NameAsc -> { list.sortedBy { it.title } }
            NameDesc -> { list.sortedByDescending { it.dateOfCreation } }
            CatchesDesc -> { list.sortedByDescending { it.catchesCount } }
        }
    }
}

enum class CatchesSortValues(override val stringRes: Int) : StringOperation {
    Default (R.string.default_word),
    TimeAsc (R.string.time_asc),
    TimeDesc (R.string.time_desc),
    NameAsc (R.string.name_asc),
    NameDesc (R.string.name_desc),
    FishDesc (R.string.fish_desc);

    fun sort(list: List<UserCatch>): List<UserCatch> {
        return when (this) {
            Default -> list
            TimeAsc -> { list.sortedBy { it.date } }
            TimeDesc -> { list.sortedByDescending { it.date } }
            NameAsc -> { list.sortedBy { it.fishType } }
            NameDesc -> { list.sortedByDescending { it.fishType } }
            FishDesc -> { list.sortedByDescending { it.fishAmount } }
        }
    }
}
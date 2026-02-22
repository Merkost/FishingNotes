package com.mobileprism.fishing.ui.utils.enums

import androidx.annotation.StringRes
import com.google.firebase.firestore.Query
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker

enum class PlacesSortValues(@StringRes override val stringRes: Int) : StringOperation {
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

    fun toFirestoreOrder(): Pair<String, Query.Direction> {
        return when (this) {
            Default, TimeDesc -> "dateOfCreation" to Query.Direction.DESCENDING
            TimeAsc -> "dateOfCreation" to Query.Direction.ASCENDING
            NameAsc -> "title" to Query.Direction.ASCENDING
            NameDesc -> "title" to Query.Direction.DESCENDING
            CatchesDesc -> "catchesCount" to Query.Direction.DESCENDING
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

    fun toFirestoreOrder(): Pair<String, Query.Direction> {
        return when (this) {
            Default, TimeDesc -> "date" to Query.Direction.DESCENDING
            TimeAsc -> "date" to Query.Direction.ASCENDING
            NameAsc -> "fishType" to Query.Direction.ASCENDING
            NameDesc -> "fishType" to Query.Direction.DESCENDING
            FishDesc -> "fishAmount" to Query.Direction.DESCENDING
        }
    }
}
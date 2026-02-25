package com.mobileprism.fishing.ui.utils.enums

import androidx.annotation.StringRes
import com.google.firebase.firestore.Query
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.common.CatchesSortValues
import com.mobileprism.fishing.domain.entity.common.PlacesSortValues
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker

@get:StringRes
val PlacesSortValues.stringRes: Int get() = when (this) {
    PlacesSortValues.Default -> R.string.default_word
    PlacesSortValues.TimeAsc -> R.string.time_asc
    PlacesSortValues.TimeDesc -> R.string.time_desc
    PlacesSortValues.NameAsc -> R.string.name_asc
    PlacesSortValues.NameDesc -> R.string.name_desc
    PlacesSortValues.CatchesDesc -> R.string.catches_desc
}

fun PlacesSortValues.sort(list: List<UserMapMarker>): List<UserMapMarker> {
    return when (this) {
        PlacesSortValues.Default -> list
        PlacesSortValues.TimeAsc -> list.sortedBy { it.dateOfCreation }
        PlacesSortValues.TimeDesc -> list.sortedByDescending { it.dateOfCreation }
        PlacesSortValues.NameAsc -> list.sortedBy { it.title }
        PlacesSortValues.NameDesc -> list.sortedByDescending { it.dateOfCreation }
        PlacesSortValues.CatchesDesc -> list.sortedByDescending { it.catchesCount }
    }
}

fun PlacesSortValues.toFirestoreOrder(): Pair<String, Query.Direction> {
    return when (this) {
        PlacesSortValues.Default, PlacesSortValues.TimeDesc -> "dateOfCreation" to Query.Direction.DESCENDING
        PlacesSortValues.TimeAsc -> "dateOfCreation" to Query.Direction.ASCENDING
        PlacesSortValues.NameAsc -> "title" to Query.Direction.ASCENDING
        PlacesSortValues.NameDesc -> "title" to Query.Direction.DESCENDING
        PlacesSortValues.CatchesDesc -> "catchesCount" to Query.Direction.DESCENDING
    }
}

@get:StringRes
val CatchesSortValues.stringRes: Int get() = when (this) {
    CatchesSortValues.Default -> R.string.default_word
    CatchesSortValues.TimeAsc -> R.string.time_asc
    CatchesSortValues.TimeDesc -> R.string.time_desc
    CatchesSortValues.NameAsc -> R.string.name_asc
    CatchesSortValues.NameDesc -> R.string.name_desc
    CatchesSortValues.FishDesc -> R.string.fish_desc
}

fun CatchesSortValues.sort(list: List<UserCatch>): List<UserCatch> {
    return when (this) {
        CatchesSortValues.Default -> list
        CatchesSortValues.TimeAsc -> list.sortedBy { it.date }
        CatchesSortValues.TimeDesc -> list.sortedByDescending { it.date }
        CatchesSortValues.NameAsc -> list.sortedBy { it.fishType }
        CatchesSortValues.NameDesc -> list.sortedByDescending { it.fishType }
        CatchesSortValues.FishDesc -> list.sortedByDescending { it.fishAmount }
    }
}

fun CatchesSortValues.toFirestoreOrder(): Pair<String, Query.Direction> {
    return when (this) {
        CatchesSortValues.Default, CatchesSortValues.TimeDesc -> "date" to Query.Direction.DESCENDING
        CatchesSortValues.TimeAsc -> "date" to Query.Direction.ASCENDING
        CatchesSortValues.NameAsc -> "fishType" to Query.Direction.ASCENDING
        CatchesSortValues.NameDesc -> "fishType" to Query.Direction.DESCENDING
        CatchesSortValues.FishDesc -> "fishAmount" to Query.Direction.DESCENDING
    }
}

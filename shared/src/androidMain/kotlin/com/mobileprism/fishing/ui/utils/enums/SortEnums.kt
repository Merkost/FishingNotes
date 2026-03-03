@file:JvmName("SortEnumsAndroid")

package com.mobileprism.fishing.ui.utils.enums

import com.google.firebase.firestore.Query
import com.mobileprism.fishing.domain.entity.common.CatchesSortValues
import com.mobileprism.fishing.domain.entity.common.PlacesSortValues

fun PlacesSortValues.toFirestoreOrder(): Pair<String, Query.Direction> {
    return when (this) {
        PlacesSortValues.Default, PlacesSortValues.TimeDesc -> "dateOfCreation" to Query.Direction.DESCENDING
        PlacesSortValues.TimeAsc -> "dateOfCreation" to Query.Direction.ASCENDING
        PlacesSortValues.NameAsc -> "title" to Query.Direction.ASCENDING
        PlacesSortValues.NameDesc -> "title" to Query.Direction.DESCENDING
        PlacesSortValues.CatchesDesc -> "catchesCount" to Query.Direction.DESCENDING
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

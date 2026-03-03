package com.mobileprism.fishing.ui.utils.enums

import com.mobileprism.fishing.domain.entity.common.CatchesSortValues
import com.mobileprism.fishing.domain.entity.common.PlacesSortValues
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource

val PlacesSortValues.stringRes: StringResource get() = when (this) {
    PlacesSortValues.Default -> Res.string.default_word
    PlacesSortValues.TimeAsc -> Res.string.time_asc
    PlacesSortValues.TimeDesc -> Res.string.time_desc
    PlacesSortValues.NameAsc -> Res.string.name_asc
    PlacesSortValues.NameDesc -> Res.string.name_desc
    PlacesSortValues.CatchesDesc -> Res.string.catches_desc
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

val CatchesSortValues.stringRes: StringResource get() = when (this) {
    CatchesSortValues.Default -> Res.string.default_word
    CatchesSortValues.TimeAsc -> Res.string.time_asc
    CatchesSortValues.TimeDesc -> Res.string.time_desc
    CatchesSortValues.NameAsc -> Res.string.name_asc
    CatchesSortValues.NameDesc -> Res.string.name_desc
    CatchesSortValues.FishDesc -> Res.string.fish_desc
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

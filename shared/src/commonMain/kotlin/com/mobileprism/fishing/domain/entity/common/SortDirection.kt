package com.mobileprism.fishing.domain.entity.common

import dev.gitlive.firebase.firestore.Direction

enum class SortDirection { ASCENDING, DESCENDING }

fun SortDirection.toGitliveDirection(): Direction = when (this) {
    SortDirection.ASCENDING -> Direction.ASCENDING
    SortDirection.DESCENDING -> Direction.DESCENDING
}

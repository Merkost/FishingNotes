package com.mobileprism.fishing.ui.home.notes

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.utils.time.toDateTextMonth

fun getDatesList(catches: List<UserCatch>): List<String> {
    val dates = mutableListOf<String>()
    catches.forEach { userCatch ->
        val date = userCatch.date.toDateTextMonth()
        if (!dates.contains(date)) {
            dates.add(date)
        }
    }
    return dates
}

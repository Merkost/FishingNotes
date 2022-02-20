package com.mobileprism.fishing.ui.home.profile

import com.mobileprism.fishing.model.entity.content.UserCatch
import com.mobileprism.fishing.model.entity.content.UserMapMarker

fun findBestCatch(catches: List<UserCatch>) = catches.maxByOrNull { it.fishWeight }
fun findFavoritePlace(places: List<UserMapMarker>) = places.maxByOrNull { it.catchesCount }
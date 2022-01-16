package com.joesemper.fishing.compose.ui.home.profile

import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker

fun findBestCatch(catches: List<UserCatch>) = catches.maxByOrNull { it.fishWeight }
fun findFavoritePlace(places: List<UserMapMarker>) = places.maxByOrNull { it.catchesCount }
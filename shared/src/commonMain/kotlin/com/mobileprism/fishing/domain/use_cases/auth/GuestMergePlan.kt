package com.mobileprism.fishing.domain.use_cases.auth

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker

data class GuestMergePlan(
    val markersToCopy: List<UserMapMarker>,
    val catchesToCopy: List<UserCatch>,
    val alreadyPresent: Int,
)

fun planGuestMerge(
    guestMarkers: List<UserMapMarker>,
    existingMarkers: List<UserMapMarker>,
    guestCatches: List<UserCatch>,
    existingCatches: List<UserCatch>,
): GuestMergePlan {
    val existingMarkerIds = existingMarkers.mapTo(HashSet()) { it.id }
    val existingCatchIds = existingCatches.mapTo(HashSet()) { it.id }
    val markersToCopy = guestMarkers.filter { it.id !in existingMarkerIds }
    val catchesToCopy = guestCatches.filter { it.id !in existingCatchIds }
    val alreadyPresent = (guestMarkers.size - markersToCopy.size) +
        (guestCatches.size - catchesToCopy.size)
    return GuestMergePlan(markersToCopy, catchesToCopy, alreadyPresent)
}

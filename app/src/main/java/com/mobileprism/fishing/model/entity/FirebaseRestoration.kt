package com.mobileprism.fishing.model.entity

import android.os.Parcelable
import com.mobileprism.fishing.domain.entity.common.FishingFirebaseUser
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import kotlinx.parcelize.Parcelize

@Parcelize
data class FirebaseRestoration(
    val firebaseUser: FishingFirebaseUser,
    val userMarkers: List<UserMapMarker>,
    val userCatches: List<UserCatch>
) : Parcelable

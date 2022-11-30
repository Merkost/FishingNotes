package com.mobileprism.fishing.domain.entity.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class FishingFirebaseUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
): Parcelable


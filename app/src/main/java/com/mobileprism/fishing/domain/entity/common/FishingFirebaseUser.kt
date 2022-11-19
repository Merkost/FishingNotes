package com.mobileprism.fishing.domain.entity.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class FishingFirebaseUser(
    val uid: String = "",
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
): Parcelable


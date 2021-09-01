package com.joesemper.fishing.data.entity.common

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.joesemper.fishing.R
import kotlinx.android.parcel.Parcelize


@Parcelize
data class User(
    val userId: String = "1",
    val userName: String = "Anonymous",
    val isAnonymous: Boolean = true,
    val userPic: String? = null,
): Parcelable
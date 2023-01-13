package com.mobileprism.fishing.model.entity

import android.os.Parcelable
import androidx.annotation.StringRes
import com.mobileprism.fishing.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class FishingResponse(
    val success: Boolean = false,
    val fishingCode: FishingCodes = FishingCodes.UNKNOWN_ERROR,
    val httpCode: Int = 0,
    val description: String = "",
) : Parcelable

@Parcelize
enum class FishingCodes(@StringRes val stringRes: Int) : Parcelable {
    SUCCESS(R.string.success),
    UNKNOWN_ERROR(R.string.fc_unknown_error),
    USER_NOT_FOUND(R.string.fc_user_not_found),
    USERNAME_NOT_FOUND(R.string.fc_username_not_found),
    INVALID_CREDENTIALS(R.string.fc_invalid_credentials),
    OTP_NOT_FOUND(R.string.fc_invalid_otp),
    NETWORK_ERROR(R.string.fc_network_error),
    OTP_ATTEMPTS_EXCEEDED(R.string.fc_otp_attempts_exceeded);
}

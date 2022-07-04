package com.mobileprism.fishing.model.entity.user

data class UserApiResponse(
    val token: String,
    val user: UserData
)
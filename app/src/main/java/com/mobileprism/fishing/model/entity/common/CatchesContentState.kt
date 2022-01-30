package com.mobileprism.fishing.model.entity.common

import com.mobileprism.fishing.model.entity.content.UserCatch

data class CatchesContentState(
    val added: MutableList<UserCatch> = mutableListOf(),
    val deleted: MutableList<UserCatch> = mutableListOf(),
    val modified: MutableList<UserCatch> = mutableListOf(),
)
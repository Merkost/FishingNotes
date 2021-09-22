package com.joesemper.fishing.model.entity.common

import com.joesemper.fishing.model.entity.content.UserCatch

data class CatchesContentState(
    val added: MutableList<UserCatch> = mutableListOf(),
    val deleted: MutableList<UserCatch> = mutableListOf()
)
package com.joesemper.fishing.domain.viewstates

import com.joesemper.fishing.model.entity.content.UserCatch

sealed class ContentState {
    class Added(val content: List<UserCatch>) : ContentState()
    class Deleted(val content: List<UserCatch>) : ContentState()
}
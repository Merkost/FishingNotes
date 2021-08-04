package com.joesemper.fishing.viewmodels.viewstates

import com.joesemper.fishing.data.entity.common.User

sealed class GroupsViewState {
    class Loading(val progress: Int?): GroupsViewState()
    class Success(val userData: User): GroupsViewState()
    class Error(val error: Throwable): GroupsViewState()
}
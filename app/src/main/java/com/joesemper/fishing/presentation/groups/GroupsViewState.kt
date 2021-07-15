package com.joesemper.fishing.presentation.groups

import com.joesemper.fishing.model.common.User

sealed class GroupsViewState {
    class Loading(val progress: Int?): GroupsViewState()
    class Success(val userData: User): GroupsViewState()
    class Error(val error: Throwable): GroupsViewState()
}
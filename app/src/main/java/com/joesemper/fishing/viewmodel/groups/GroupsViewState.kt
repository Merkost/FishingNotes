package com.joesemper.fishing.viewmodel.groups

import com.joesemper.fishing.viewmodel.base.ViewState
import com.joesemper.fishing.model.entity.common.User

sealed class GroupsViewState: ViewState {
    class Loading(val progress: Int?): GroupsViewState()
    class Success(val userData: User): GroupsViewState()
    class Error(val error: Throwable): GroupsViewState()
}
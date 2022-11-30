package com.mobileprism.fishing.model.utils

import com.mobileprism.fishing.model.datastore.UserDatastore
import com.mobileprism.fishing.model.entity.user.UserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

object UserHandler {
    val coroutineScope = CoroutineScope(SupervisorJob())

    val _currentUser = MutableStateFlow(UserData())
    val currentUser = _currentUser.asStateFlow()
}
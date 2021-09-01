package com.joesemper.fishing.viewmodels

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import com.joesemper.fishing.data.repository.UserRepository
import java.util.concurrent.Flow

class UserViewModel(private val repository: UserRepository): ViewModel() {

    fun getCurrentUser() = repository.currentUser

    suspend fun logoutCurrentUser() = repository.logoutCurrentUser()

}
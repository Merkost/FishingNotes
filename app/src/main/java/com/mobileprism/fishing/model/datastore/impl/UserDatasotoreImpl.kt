package com.mobileprism.fishing.model.datastore.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.model.datastore.UserDatastore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserDatasotoreImpl(private val context: Context): UserDatastore {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("appSettings")

        private val USER_UID_KEY = stringPreferencesKey("user_uid")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_PHOTOURL_KEY = stringPreferencesKey("user_photoUrl")
        private val USER_LOGIN_KEY = stringPreferencesKey("user_login")
        private val USER_REGISTERDATE_KEY = longPreferencesKey("user_regDate")
    }

    //get the saved value
    override val getUser: Flow<User> = context.dataStore.data
        .map { preferences ->
                User(
                    uid = preferences[USER_UID_KEY] ?: "",
                    email = preferences[USER_EMAIL_KEY] ?: "",
                    displayName = preferences[USER_NAME_KEY] ?: "",
                    photoUrl = preferences[USER_PHOTOURL_KEY] ?: "",
                    login = preferences[USER_LOGIN_KEY] ?: "",
                    registerDate = preferences[USER_REGISTERDATE_KEY] ?: 0,
                )
        }

    override suspend fun saveUser(user: User) {
        context.dataStore.edit { preferences ->
            user.apply {
                preferences[USER_UID_KEY] = uid
                preferences[USER_EMAIL_KEY] = email
                preferences[USER_NAME_KEY] = displayName
                preferences[USER_PHOTOURL_KEY] = photoUrl
                preferences[USER_LOGIN_KEY] = login
                preferences[USER_REGISTERDATE_KEY] = registerDate
            }
        }
    }

}
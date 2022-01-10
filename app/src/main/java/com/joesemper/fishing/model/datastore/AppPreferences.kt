package com.joesemper.fishing.model.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.joesemper.fishing.model.entity.common.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppPreferences(private val context: Context) {

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
    val userValue: Flow<User?> = context.dataStore.data
        .map { preferences ->
            if (preferences[USER_UID_KEY] == null) return@map null
            else {
                User(
                    uid = preferences[USER_UID_KEY] ?: "",
                    email = preferences[USER_EMAIL_KEY] ?: "",
                    displayName = preferences[USER_NAME_KEY] ?: "",
                    photoUrl = preferences[USER_PHOTOURL_KEY] ?: "",
                    login = preferences[USER_LOGIN_KEY] ?: "",
                    registerDate = preferences[USER_REGISTERDATE_KEY] ?: 0,
                )
            }
        }

    suspend fun saveUserValue(user: User) {
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
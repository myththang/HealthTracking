package com.fpt.edu.healthtracking.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

data class TokenRequest(
    val accessToken: String,
    val refreshToken: String
)

data class AuthState(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val email: String? = null,
    val password: String? = null,
    val isLoggedIn: Boolean = false
)

open class UserPreferences(context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "my_data_store")
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("key_access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("key_refresh_token")
        private val KEY_EMAIL = stringPreferencesKey("key_email")
        private val KEY_PASSWORD = stringPreferencesKey("key_password")
    }

    private val applicationContext = context.applicationContext
    private val dataStore = applicationContext.dataStore

    suspend fun saveAuthTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = accessToken
            preferences[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun saveCredentials(email: String, password: String) {
        dataStore.edit { preferences ->
            preferences[KEY_EMAIL] = email
            preferences[KEY_PASSWORD] = password
        }
    }

    val authStateFlow: Flow<AuthState> = dataStore.data.map { preferences ->
        AuthState(
            accessToken = preferences[KEY_ACCESS_TOKEN],
            refreshToken = preferences[KEY_REFRESH_TOKEN],
            email = preferences[KEY_EMAIL],
            password = preferences[KEY_PASSWORD],
            isLoggedIn = preferences[KEY_ACCESS_TOKEN] != null
        )
    }

    suspend fun clearAuth() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_ACCESS_TOKEN)
            preferences.remove(KEY_REFRESH_TOKEN)
            preferences.remove(KEY_EMAIL)
            preferences.remove(KEY_PASSWORD)
        }
    }
}
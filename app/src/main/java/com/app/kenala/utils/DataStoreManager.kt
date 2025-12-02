package com.app.kenala.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extension untuk membuat DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kenala_preferences")

class DataStoreManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
        private val LOCATION_KEY = booleanPreferencesKey("location_enabled")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
    }

    // Flow untuk membaca token
    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    // Flow untuk membaca user ID
    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    // Simpan data login
    suspend fun saveAuthData(token: String, userId: String, userName: String, userEmail: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[USER_NAME_KEY] = userName
            preferences[USER_EMAIL_KEY] = userEmail
        }
    }

    // Hapus semua data (logout)
    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Cek apakah user sudah login
    suspend fun isLoggedIn(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[TOKEN_KEY] != null
    }

    // Get token
    suspend fun getToken(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[TOKEN_KEY]
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[NOTIFICATIONS_KEY] ?: true // Default: Nyala
    }

    val locationEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[LOCATION_KEY] ?: true // Default: Nyala
    }

    val darkModeEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[DARK_MODE_KEY] ?: false // Default: Mati (Light Mode)
    }

    // Menyimpan pengaturan
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_KEY] = enabled }
    }

    suspend fun setLocationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[LOCATION_KEY] = enabled }
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE_KEY] = enabled }
    }
}